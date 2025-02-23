from tsp_solver.greedy import solve_tsp
import traceback
import multiprocessing
import pyproj
import argparse
import numpy as np
from scipy.spatial import cKDTree
from shapely.geometry import Point, LineString
import pickle
import networkx as nx
import pandas as pd
import geopandas as gpd
import math
import requests
from fastapi import HTTPException
from fastapi.middleware.cors import CORSMiddleware


# 根据用户需求五元组、用户偏好以及初步筛选POI筛选得到最终POI
import requests
from urllib.parse import quote
import json


from fastapi import FastAPI
from pydantic import BaseModel

# 创建FastAPI实例
app = FastAPI()

# 允许所有域、所有方法（GET, POST等）、所有标头
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# 定义请求体模型
class InputData(BaseModel):
    id: str
    dialogId: str


def getFinalPOI(startPOI, endPOI, startTime, endTime, numPOI, userId):
    """
    :param startPOI: 用户需求五元组-起点
    :param endPOI: 用户需求五元组-终点
    :param startTime: 用户需求五元组-起始时间
    :param endTime: 用户需求五元组-终止时间
    :param numPOI: 用户需求五元组-途径点个数
    :param userId: 用户ID
    :return: 返回给用户推荐的 numPOI 个 POI 信息
    """
    # 调用用户偏好获取接口获取用户的旅行偏好
    url = f"http://backend:7778/preferences?id={userId}"
    payload = {}
    headers = {"User-Agent": "Apifox/1.0.0 (https://apifox.com)"}
    response_pre = requests.request("GET", url, headers=headers, data=payload)
    # 检查请求是否成功
    if response_pre.status_code == 200:
        response_pre_json = response_pre.json()
        # 获取用户的旅行偏好
        interested_places = response_pre_json.get("data", {}).get("interestedPlaces")
        interested_ways = response_pre_json.get("data", {}).get("interestedWays")
        travel_companion = response_pre_json.get("data", {}).get("travelCompanion")
    else:
        print(f"Request failed with status code: {response_pre.status_code}")
        # 如果请求失败，可以设置默认值或终止程序
        interested_places = None
        interested_ways = None
        travel_companion = None

    # 如果用户偏好为空，可以设置默认值
    if not interested_places:
        interested_places = "自然风光，美食"
    if not interested_ways:
        interested_ways = "常规路线"
    if not travel_companion:
        travel_companion = "恋人"

    # 调用 POI 初步筛选接口获得初步 POI 候选集
    start_name = quote(startPOI)
    end_name = quote(endPOI)
    poi_filter_url = f"http://backend:7778/position/filter?begin={start_name}&end={end_name}&num=23"
    payload = {}
    headers.update(
        {"Accept": "*/*", "Host": "backend:7778", "Connection": "keep-alive"}
    )
    response_poi = requests.request(
        "GET", poi_filter_url, headers=headers, data=payload
    )
    selected_poi = response_poi.json().get("data", {})
    # print(selected_poi)

    # 构建 prompt，根据用户需求五元组、用户偏好以及候选 POI 得到最终推荐给用户的 POI
    preference = f"{{感兴趣的旅行地点：{interested_places}；感兴趣的旅行方式：{interested_ways}；旅行伙伴：{travel_companion}}};"
    input_info = (
        f"1.出发地点：{startPOI}；2.出发时间：{startTime}；3.终止地点：{endPOI}；"
        f"4.终止时间：{endTime}；5.途径点个数：{numPOI}个；6.旅行偏好：{preference}；"
        f"7.可参考的地点信息为：{selected_poi}"
    )
    prompt = f"""
    TASK：作为旅游行程规划师，假设你充分了解北京市的所有地点以及交通信息，且十分擅长于根据不同人群的需求安排合理的游玩地点和时间。请根据给定信息推荐{numPOI}个POI地点，构成一条顺畅且完整的旅行路线。
    REQUEST：规划一个旅游路线，包括POI地点序列。请根据以下信息规划路线：{input_info}
    ACTION：
    1. 确定本次行程的大概活动范围，根据起终点的位置规划路线。
    2. 根据游客的偏好找到所有可能感兴趣的景点、美食、购物以及休闲娱乐场所等。
    3. 综合考虑行程起终时间、交通时间、地点之间是否顺路等因素，规划一条合理的路线。
    4. 安排就餐地点时尽量选择距离参观景点一公里以内的地点。
    5. 你只需要返回 {numPOI} 个你认为最合适的地点即可，不要多于 {numPOI} 个也不要少于 {numPOI} 个。
    EXAMPLE：
    以下是一个推荐POI地点的示例，最终输出应为 JSON 格式文本：
    {{
        "pois":[
            {{
                "name": "[地点名称]",
                "longitude": [地点经度],
                "latitude": [地点纬度]
            }},
            ...
        ]
    }}
    """
    encoded_prompt = quote(prompt)

    # 创建对话的 API
    create_dialog_url = "http://backend:7778/dialog"
    create_payload = {}
    create_headers = headers.copy()
    create_headers.update(
        {"Accept": "*/*", "Host": "backend:7778", "Connection": "keep-alive"}
    )

    # 设置参数，可以修改 name 和 userId 的值
    params = {"userId": userId, "name": "流式输出尝试"}  # 确保 userId 一致

    # 发送创建对话的请求
    response = requests.request(
        "POST",
        create_dialog_url,
        headers=create_headers,
        params=params,
        data=create_payload,
    )
    create_result = response.json()

    # 获取返回的 dialogId
    dialog_id = create_result["data"]["id"]
    print(f"创建的对话 ID 为：{dialog_id}")

    # 进行对话的 API
    ask_url = f"http://backend:7778/spark/ask?input={encoded_prompt}&dialogId={dialog_id}&userId={userId}"

    ask_payload = {}
    ask_headers = headers.copy()

    # 发送对话请求
    response = requests.request("POST", ask_url, headers=ask_headers, data=ask_payload)

    # 打印响应内容
    print("对话响应：")
    print(response.text)

    # 解析响应内容，获取 POI 信息
    ask_result = response.json()
    poi_content = ask_result["data"]["output"]

    print(poi_content)
    print(poi_content.strip())
    # 删除```json
    poi_content=poi_content.replace("```json", "")
    poi_content=poi_content.replace("```", "")
    print(poi_content)
    # 将字符串转换为字典
    try:
        final_poi = json.loads(poi_content)
    except json.JSONDecodeError as e:
        print("解析 POI 内容时出错：", e)
        final_poi = None


    """
    返回如下的 POI 集合(final_poi 是一个字典)：
    {
        "pois": [
            {
                "name": "北京故宫博物院",
                "longitude": 116.403414,
                "latitude": 39.924091
            },
            ...
        ]
    }
    """
    return final_poi


def get_travel_plan(input_text):

    # 创建对话的 API
    create_dialog_url = "http://backend:7778/dialog"
    create_payload = {}
    create_headers = {
        "User-Agent": "Apifox/1.0.0 (https://apifox.com)",
        "Accept": "*/*",
        "Host": "backend:7778",
        "Connection": "keep-alive",
    }

    # 设置参数，可以修改 name 和 userId 的值
    params = {"userId": 6, "name": "流式输出尝试"}  # 你可以修改为需要的 userId

    # 发送创建对话的请求
    response = requests.request(
        "POST",
        create_dialog_url,
        headers=create_headers,
        params=params,
        data=create_payload,
    )
    create_result = response.json()

    # 获取返回的 dialogId
    dialog_id = create_result["data"]["id"]
    print(f"创建的对话 ID 为：{dialog_id}")

    # 进行对话的 API

    ask_url = f"http://backend:7778/spark/ask?input={input_text}&dialogId={dialog_id}&userId=6"

    ask_payload = {}
    ask_headers = {
        "User-Agent": "Apifox/1.0.0 (https://apifox.com)",
        "Accept": "*/*",
        "Host": "backend:7778",
        "Connection": "keep-alive",
    }

    # 发送对话请求
    response = requests.request("POST", ask_url, headers=ask_headers, data=ask_payload)

    print(response.text)

    response_data = response.json()

    if response_data.get("code") == 1:
        content = response_data["data"]["output"]
        print(content)

        # Convert the JSON content to a Python dictionary
        travel_plan = eval(content.strip().replace("```json", "").replace("```", ""))

        return travel_plan
    else:
        return None


def get_location(address):
    par = {"address": address, "key": "6a186a3cb0f3af7939876f5d9e3e32c7"}
    url = "http://restapi.amap.com/v3/geocode/geo"
    res = requests.get(url, par)
    json_data = json.loads(res.text)
    geo = json_data["geocodes"][0]["location"]
    longitude = float(geo.split(",")[0])
    latitude = float(geo.split(",")[1])
    return longitude, latitude


x_pi = 3.14159265358979324 * 3000.0 / 180.0
pi = 3.1415926535897932384626  # π
a = 6378245.0  # 长半轴
ee = 0.00669342162296594323  # 偏心率平方


def _transformlat(lng, lat):
    ret = (
        -100.0
        + 2.0 * lng
        + 3.0 * lat
        + 0.2 * lat * lat
        + 0.1 * lng * lat
        + 0.2 * math.sqrt(math.fabs(lng))
    )
    ret += (
        (20.0 * math.sin(6.0 * lng * pi) + 20.0 * math.sin(2.0 * lng * pi)) * 2.0 / 3.0
    )
    ret += (20.0 * math.sin(lat * pi) + 40.0 * math.sin(lat / 3.0 * pi)) * 2.0 / 3.0
    ret += (
        (160.0 * math.sin(lat / 12.0 * pi) + 320 * math.sin(lat * pi / 30.0))
        * 2.0
        / 3.0
    )
    return ret


def _transformlng(lng, lat):
    ret = (
        300.0
        + lng
        + 2.0 * lat
        + 0.1 * lng * lng
        + 0.1 * lng * lat
        + 0.1 * math.sqrt(math.fabs(lng))
    )
    ret += (
        (20.0 * math.sin(6.0 * lng * pi) + 20.0 * math.sin(2.0 * lng * pi)) * 2.0 / 3.0
    )
    ret += (20.0 * math.sin(lng * pi) + 40.0 * math.sin(lng / 3.0 * pi)) * 2.0 / 3.0
    ret += (
        (150.0 * math.sin(lng / 12.0 * pi) + 300.0 * math.sin(lng / 30.0 * pi))
        * 2.0
        / 3.0
    )
    return ret


def out_of_china(lng, lat):
    """
    判断是否在国内，不在国内不做偏移
    :param lng:
    :param lat:
    :return:
    """
    return not (lng > 73.66 and lng < 135.05 and lat > 3.86 and lat < 53.55)


def gcj02_to_wgs84(lng, lat):
    if out_of_china(lng, lat):
        return [lng, lat]
    dlat = _transformlat(lng - 105.0, lat - 35.0)
    dlng = _transformlng(lng - 105.0, lat - 35.0)
    radlat = lat / 180.0 * pi
    magic = math.sin(radlat)
    magic = 1 - ee * magic * magic
    sqrtmagic = math.sqrt(magic)
    dlat = (dlat * 180.0) / ((a * (1 - ee)) / (magic * sqrtmagic) * pi)
    dlng = (dlng * 180.0) / (a / sqrtmagic * math.cos(radlat) * pi)
    mglat = lat + dlat
    mglng = lng + dlng
    return [lng * 2 - mglng, lat * 2 - mglat]


def create_and_save_graph(shapefile_path, graph_pickle_path):
    print("create and save graph!")
    # 读取 Shapefile
    gdf = gpd.read_file(shapefile_path)
    print("列名:", gdf.columns)

    # 检查 GeoDataFrame 是否有 CRS
    print("原始 CRS:", gdf.crs)

    # 如果 GeoDataFrame 没有 CRS，假设它是 WGS84（经纬度坐标，EPSG:4326）
    if gdf.crs is None:
        gdf.set_crs("EPSG:4326", inplace=True)
        print("已设置 CRS 为 EPSG:4326")

    geod = pyproj.Geod(ellps="WGS84")
    gdf["length"] = gdf.geometry.apply(geod.geometry_length)

    print("列名:", gdf.columns)
    print(gdf)

    # 创建 NetworkX 图
    G = nx.Graph()
    for idx, row in gdf.iterrows():
        geom = row.geometry
        if isinstance(geom, LineString):
            coords = list(geom.coords)
            for start, end in zip(coords[:-1], coords[1:]):
                # 假设 'length' 是边的权重
                G.add_edge(start, end, weight=row["length"])
    print("start saving graph!")
    # 使用 pickle 将图形结构存储到文件
    with open(graph_pickle_path, "wb") as f:
        pickle.dump(G, f)
    return G


def load_graph(graph_pickle_path):
    # 从 pickle 文件加载图
    with open(graph_pickle_path, "rb") as f:
        G = pickle.load(f)
    return G


# 投影点到最近的路网节点
def project_points_to_graph(points, G):
    # 构建空间索引
    nodes = np.array([node for node in G.nodes])
    tree = cKDTree(nodes)

    # 投影点
    to_nearest_node_delta = []
    nearest_node_idxs = []
    for point in points:
        dist, idx = tree.query([point[0], point[1]], k=1)
        nearest_node_idxs.append(idx)
        to_nearest_node_delta.append(dist)
    return nearest_node_idxs, to_nearest_node_delta, nodes


def part_shortest_path_cal(args):
    lower, upper = args[0], args[1]
    print(f"son process ({lower},{upper})")
    distances_via_rn = np.frombuffer(shared_arr.get_obj(), dtype=np.float64).reshape(
        len(nearest_node_idxs), len(nearest_node_idxs)
    )
    cnt = 0
    for i, j in products[lower:upper]:
        try:
            path_length = nx.shortest_path_length(
                G,
                tuple(nodes[nearest_node_idxs[i]]),
                tuple(nodes[nearest_node_idxs[j]]),
                weight="weight",
            )
        except Exception as e:
            print(f"{e.args}\n======")
            print(
                f"ERROR:{tuple(nodes[nearest_node_idxs[i]])}->{tuple(nodes[nearest_node_idxs[j]])}!"
            )
            print(f"{traceback.format_exc()}")
            path_length = float("inf")

        distances_via_rn[i][j] = path_length
        cnt += 1
        print(f"\tson process ({lower},{upper}):{cnt}/{upper - lower}", end="")
        print(
            f"最短路径长度从点 {nodes[nearest_node_idxs[i]]} 到点 {nodes[nearest_node_idxs[j]]} 是：{path_length/1000} 千米"
        )
    print(f"FINISHED! son process ({lower},{upper})")


def init_pool(shared_arr_, G_, nearest_node_idxs_, products_):
    global shared_arr, G, nearest_node_idxs, products
    shared_arr = shared_arr_
    G = G_
    nearest_node_idxs = nearest_node_idxs_
    products = products_


def get_result(input_text, travel_plan):

    prompt = (
        input_text
        + "我的旅行计划是出发地点="
        + travel_plan["出发地点"]
        + ", 出发时间="
        + travel_plan["出发时间"]
        + ", 结束地点="
        + travel_plan["结束地点"]
        + ", 结束时间="
        + travel_plan["结束时间"]
        + ", 经过个数="
        + str(travel_plan["经过个数"])
        + """。你需要输出一个合理的规划，用json格式发给我。
    请注意：
    1. 返回的引号一定要用双引号"，要有回车。
    2. 地点与地点之间的通勤时间应该和我发给你的相同。
    3. 每个地点从到达时间到离开时间大概停留1-2个小时。

输出示例如下：
{
    "出发地点": "出发地点A",
    "出发时间": "hh:mm",mimami
    "途径景点": [
        {
            "到达时间": "hh:mm",
            "名称": "景点1",
            "离开时间": "hh:mm"
        },
        {
            "到达时间": "hh:mm",
            "名称": "景点2",
            "离开时间": "hh:mm"
        },
        {
            "到达时间": "hh:mm",
            "名称": "景点3",
            "离开时间": "hh:mm"
        },
        {
            "到达时间": "hh:mm",
            "名称": "景点4",
            "离开时间": "hh:mm"
        }
    ],
    "结束地点": "结束地点B",
    "结束时间": "hh:mm"
}
"""
    )
    # print(prompt)
    # 创建对话的 API
    create_dialog_url = "http://backend:7778/dialog"
    create_payload = {}
    create_headers = {
        "User-Agent": "Apifox/1.0.0 (https://apifox.com)",
        "Accept": "*/*",
        "Host": "backend:7778",
        "Connection": "keep-alive",
    }

    # 设置参数，可以修改 name 和 userId 的值
    params = {"userId": 6, "name": "流式输出尝试"}  # 你可以修改为需要的 userId

    # 发送创建对话的请求
    response = requests.request(
        "POST",
        create_dialog_url,
        headers=create_headers,
        params=params,
        data=create_payload,
    )
    create_result = response.json()

    # 获取返回的 dialogId
    dialog_id = create_result["data"]["id"]
    print(f"创建的对话 ID 为：{dialog_id}")

    # 进行对话的 API
    input_text = "你好，请问你叫什么名字？"  # 可以修改为你想要的输入
    ask_url = (
        f"http://backend:7778/spark/ask?input={prompt}&dialogId={dialog_id}&userId=6"
    )

    ask_payload = {}
    ask_headers = {
        "User-Agent": "Apifox/1.0.0 (https://apifox.com)",
        "Accept": "*/*",
        "Host": "backend:7778",
        "Connection": "keep-alive",
    }

    # 发送对话请求
    response = requests.request("POST", ask_url, headers=ask_headers, data=ask_payload)
    response_data = response.json()

    if response_data.get("code") == 1:
        content = response_data["data"]["output"]

        # 假设 content 是一个字符串，或者已经是一个字典
        if not isinstance(content, str):
            content=str(content)
            print("Content is not a string, converting to string")

        print("Content")
        print(content)
        result = eval(content.strip().replace("```json", "").replace("```", ""))

        return result

    else:
        return None


def get_description(dialogId):
    url = f"http://backend:7778/dialog/records?id={dialogId}"

    headers = {
        "User-Agent": "Apifox/1.0.0 (https://apifox.com)",
        "Accept": "*/*",
        "Host": "backend:7778",
        "Connection": "keep-alive",
    }

    response = requests.get(url, headers=headers)

    if response.status_code == 200:
        data = response.json()
        if data["code"] == 1:
            inputs = [record["input"] for record in data["data"]]
            return " ".join(inputs)
        else:
            return f"Error: {data['msg']}"
    else:
        return f"HTTP Error: {response.status_code}"


import time


def calculate_distance_matrix(points, api_key):
    shape = (len(points), len(points))
    distances = np.full(shape, np.inf)
    np.fill_diagonal(distances, 0.0)

    base_url = "https://restapi.amap.com/v3/direction/driving"
    products = [(i, j) for i in range(len(points)) for j in range(len(points)) if j > i]

    for i, j in products:
        start_point = f"{points[i][0]},{points[i][1]}"  # 经度, 纬度
        end_point = f"{points[j][0]},{points[j][1]}"
        print(f"Calculating route from {start_point} to {end_point}")

        params = {"origin": start_point, "destination": end_point, "key": api_key}

        # Debugging: print the request URL
        print(
            f"Requesting URL: {base_url}?origin={start_point}&destination={end_point}&key={api_key}"
        )

        response = requests.get(base_url, params=params)
        response_data = response.json()

        if (
            response_data["status"] == "1"
            and "paths" in response_data["route"]
            and len(response_data["route"]["paths"]) > 0
        ):
            path_length = response_data["route"]["paths"][0]["distance"]  # 距离单位为米
            distances[i, j] = path_length
            distances[j, i] = path_length  # 确保对称性
        else:
            print(f"高德API错误: {response_data['info']}")

        time.sleep(1)  # 限制请求速率

    return distances


shapefile_path = "beijing/edges.shp"  # 替换为你的 Shapefile 路径
graph_pickle_path = "beijing/networkx_beijing.pkl"  # 图形数据的存储路径

create_g = False  # 根据需要设定是否创建图形数据

if create_g:
    G = create_and_save_graph(shapefile_path, graph_pickle_path)
else:
    G = load_graph(graph_pickle_path)

import json

import folium
import requests
from fastapi import FastAPI
from folium import PolyLine


@app.get("/get-plan/")
async def get_plan(userId: int, dialogId: int):

    description = get_description(dialogId)

    print("description=" + description)

    input_text = (
        """请从下面对话中提取出五元组并以JSON格式输出："""
        + description
        + """请将行程按以下格式输出，并确保以JSON格式提供：
    { 
        "出发地点": "[出发地点]",
        "出发时间": "yyyy.mm.dd hh:mm",
        "结束地点": "[结束地点]",
        "结束时间": "yyyy.mm.dd hh:mm",
        "经过个数": [经过个数]
    }"""
    )
    travel_plan = get_travel_plan(input_text)
    print(travel_plan)
    if not travel_plan:
        raise HTTPException(status_code=400, detail="Failed to get travel plan")

    startPOI = travel_plan["出发地点"]
    endPOI = travel_plan["结束地点"]
    startTime = travel_plan["出发时间"]
    endTime = travel_plan["结束时间"]
    numPOI = travel_plan["经过个数"]
    # 如果numPOI是list，替换为int
    if type(numPOI) == list:
        numPOI= int(numPOI[0])
    else:
        numPOI = int(numPOI)

    start_coords = get_location(startPOI)
    end_coords = get_location(endPOI)

    if not start_coords or not end_coords:
        raise HTTPException(status_code=400, detail="Failed to get coordinates")

    start_coords_wgs84 = gcj02_to_wgs84(*start_coords)
    end_coords_wgs84 = gcj02_to_wgs84(*end_coords)

    poi = getFinalPOI(startPOI, endPOI, startTime, endTime, numPOI, userId)

    print(startPOI)
    print(start_coords_wgs84)
    print(poi)
    print(endPOI)
    print(end_coords_wgs84)

    if not poi:
        raise HTTPException(status_code=400, detail="Failed to get POI data")

    # 假设你已有的 start_coords_wgs84 和 end_coords_wgs84
    # 将所有坐标点统一为列表格式
    points = (
        [
            [start_coords_wgs84[0], start_coords_wgs84[1]],  # 开始坐标
        ]
        + [[poi_item["longitude"], poi_item["latitude"]] for poi_item in poi["pois"]]
        + [
            [end_coords_wgs84[0], end_coords_wgs84[1]],  # 结束坐标
        ]
    )

    # 确保每个点都是列表
    points = [list(point) if isinstance(point, tuple) else point for point in points]

    nearest_node_idxs, to_nearest_node_delta, nodes = project_points_to_graph(points, G)

    shape = (len(points), len(points))
    distances_via_rn = np.full(shape, np.inf)
    np.fill_diagonal(distances_via_rn, 0.0)

    products = [(i, j) for i in range(len(points)) for j in range(len(points)) if j > i]

    # 高德API配置
    AMAP_API_KEY = "6a186a3cb0f3af7939876f5d9e3e32c7"
    base_url = "https://restapi.amap.com/v3/direction/driving"

    distances_via_rn = calculate_distance_matrix(points, AMAP_API_KEY)

    path = solve_tsp(distances_via_rn)

    speed_m_per_m = 20 * 1000 / 60
    commute_times = []
    for i in range(len(path) - 1):
        from_idx = path[i]
        to_idx = path[i + 1]
        distance = distances_via_rn[max(from_idx, to_idx), min(from_idx, to_idx)]
        if distance == np.inf:
            commute_times.append(np.inf)
        else:
            time_in_minutes = math.ceil(distance / speed_m_per_m) + 2
            commute_times.append(time_in_minutes)

    poi_names = (
        [travel_plan["出发地点"]]
        + [poi_item["name"] for poi_item in poi["pois"]]
        + [travel_plan["结束地点"]]
    )
    output_string = ""

    for i in range(len(commute_times)):
        from_name = poi_names[path[i]]
        to_name = poi_names[path[i + 1]]
        time = commute_times[i]
        if time == np.inf:
            output_string += f"从 {from_name} 到 {to_name} 的通勤时间为 无法到达\n"
        else:
            output_string += f"从 {from_name} 到 {to_name} 需要 {time} 分钟\n"

    result = get_result(output_string, travel_plan)

    poi_dict = {
        poi["name"]: {"longitude": poi["longitude"], "latitude": poi["latitude"]}
        for poi in poi["pois"]
    }

    result["出发地点经纬度"] = {
        "longitude": start_coords_wgs84[0],
        "latitude": start_coords_wgs84[1],
    }
    result["结束地点经纬度"] = {
        "longitude": end_coords_wgs84[0],
        "latitude": end_coords_wgs84[1],
    }

    for spot in result["途径景点"]:
        name = spot["名称"]
        if name in poi_dict:
            spot.update(poi_dict[name])

    result = json.dumps(result, ensure_ascii=False, indent=4)
    print(result)
    return result


import os
import sys

if __name__ == "__main__":
    # 在Windows上使用start命令启动Uvicorn
    os.system(
        "start /B uvicorn get_plan:app --reload --host 0.0.0.0 --port 7780 > plan.log 2>&1"
    )
