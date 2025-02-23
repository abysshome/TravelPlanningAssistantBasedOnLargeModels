package org.example.backend_1.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.example.backend_1.pojo.Poi;
import org.example.backend_1.pojo.Position;
import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GeodeticCurve;
import org.gavaghan.geodesy.GlobalCoordinates;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class SelectPOI {
    public static double distance_weight = 0.5;
    public static double popularity_weight = 0.5;

    public static void main(String[] args) {
        System.out.println(getLL("北京理工大学").toString());
    }

    /**
     * 筛选方法一：综合distance和popularity进行排名，获取前num个poi数据集
     *
     * @param pois  待筛选的poi数据集
     * @param begin 起点
     * @param end   终点
     * @param num   目标数量
     * @return 筛选后的前num个poi
     */
    public static ArrayList<Poi> Select_1(ArrayList<Poi> pois, Position begin, Position end, Integer num) {
        if (num > pois.size()) {
            num = pois.size();
        } else if (num < 0) {
            num = 0;
        }

        Poi middle = new Poi();
        middle.latitude = (begin.getLatitude() + end.getLatitude()) / 2.0;
        middle.longitude = (begin.getLongitude() + end.getLongitude()) / 2.0;
        ArrayList<Position> res = new ArrayList<>();
        for (Poi poi : pois) {
            poi.score = CalculateScore(poi, middle);
        }
        pois.sort(new Comparator<Poi>() {
            @Override
            public int compare(Poi o1, Poi o2) {
                return Double.compare(o2.score, o1.score);
            }
        });
        List<Poi> selected = pois.subList(0, num);
        //去除重复的起点和终点
        selected.removeIf(poi -> poi.name.contains(begin.getName()) || begin.getName().contains(poi.name) || poi.name.contains(end.getName()) || end.getName().contains(poi.name));
        return new ArrayList<>(selected);
    }

    /**
     * 计算poi得分用户后续排序
     *
     * @param p   待计算的poi
     * @param mid begin和end的终点
     * @return 综合distance和popularity的score
     */
    public static double CalculateScore(Poi p, Poi mid) {
        double distance = getDistance(p, mid, Ellipsoid.WGS84);
//        System.out.println(Math.exp(-0.001*Math.pow(distance,0.75)));
        double popularity = p.popularity;
        return distance_weight * (Math.exp(-0.001 * Math.pow(distance, 0.75))) + popularity_weight * 2.5 * popularity;
    }

    public static boolean dotProduct(Position a, Position b, Position p) {
        double x1 = p.getLatitude() - a.getLatitude();
        double y1 = p.getLongitude() - a.getLongitude();
        double x2 = p.getLatitude() - b.getLatitude();
        double y2 = p.getLongitude() - b.getLongitude();
        return x1 * x2 + y1 * y2 < 0;
    }

    /**
     * 计算到中点的距离
     *
     * @param begin     起点
     * @param end       终点
     * @param ellipsoid 计算方式
     * @return 距离
     */
    public static double getDistance(Poi begin, Poi end, Ellipsoid ellipsoid) {
        // 创建GeodeticCalculator，调用计算方法，传入坐标系、经纬度用于计算距离
        GlobalCoordinates firstPoint = new GlobalCoordinates(begin.latitude, begin.longitude);
        GlobalCoordinates secondPoint = new GlobalCoordinates(end.latitude, end.longitude);
        GeodeticCurve geoCurve = new GeodeticCalculator().calculateGeodeticCurve(ellipsoid, firstPoint, secondPoint);
        return geoCurve.getEllipsoidalDistance();
    }

    /**
     * 将地名转换为经纬度
     *
     * @param address 地名
     * @return 经纬度
     */
    public static Position getLL(String address) {
        String getUrl = "http://restapi.amap.com/v3/geocode/geo?key=4160b1c7870ea729a80b4cb46d25490e&address=" + address;
        String location = "";
        try {
            URL url = new URL(getUrl);//把字符串转换为url请求地址
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            //获取输入六
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            connection.disconnect();
            JSONObject a = JSON.parseObject(sb.toString());
            JSONArray sddressArr = JSON.parseArray(a.get("geocodes").toString());
            JSONObject c = JSON.parseObject(sddressArr.get(0).toString());
            location = c.get("location").toString();
        } catch (IndexOutOfBoundsException index) {
            throw new IndexOutOfBoundsException();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("失败!");
        }
        String[] strings = location.split(",");
        Position res = new Position();
        double[] doubles = CoordinateTransform.transformGCJ02ToWGS84(Double.parseDouble(strings[0]), Double.parseDouble(strings[1]));
        res.setLongitude(doubles[0]);
        res.setLatitude(doubles[1]);
        res.setName(address);
        return res;
    }
}
