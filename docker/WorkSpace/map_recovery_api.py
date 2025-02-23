import sys
sys.path.append('../')
from flask import Flask, request
from flask import jsonify
from common.road_network import load_rn_shp
from common.spatial_func import distance, SPoint
from common.trajectory.map_matching.candidate_point import get_candidates
import networkx as nx
import math
from heapq import heappop, heappush
app = Flask(__name__)


def to_wkt(coords):
    wkt = 'LINESTRING ('
    for coord in coords:
        wkt += '{} {}, '.format(coord.lng, coord.lat)
    wkt = wkt[:-2] + ')'
    return wkt


class PathElement:
    def __init__(self, eid, coords, speed, dist):
        self.eid = eid
        self.coords = coords
        self.speed = speed
        self.time = dist / speed

    def to_dict(self):
        path = []
        for coord in self.coords:
            path.append('{} {}'.format(coord.lng, coord.lat))
        return {'eid': str(self.eid), 'speed': self.speed, 'transitTime': self.time, 'coordinate': path}


def find_fastest_path(rn, prev_candi_pt, cur_candi_pt):
    # case 1, on the same road
    if prev_candi_pt.eid == cur_candi_pt.eid:
        pre_u, pre_v = rn.edge_idx[prev_candi_pt.eid]
        pre_speed = rn[pre_u][pre_v]['speed']
        return math.fabs(cur_candi_pt.offset - prev_candi_pt.offset) / pre_speed, []
    # case 2, on different roads
    else:
        pre_u, pre_v = rn.edge_idx[prev_candi_pt.eid]
        cur_u, cur_v = rn.edge_idx[cur_candi_pt.eid]
        pre_speed = rn[pre_u][pre_v]['speed']
        cur_speed = rn[cur_u][cur_v]['speed']
        min_time = float('inf')
        fastest_path = None
        paths = []
        # prev_u -> cur_u
        try:
            paths.append(get_fastest_path_with_time(rn, pre_u, cur_u, prev_candi_pt.offset / pre_speed,
                                                    cur_candi_pt.offset / cur_speed))
        except nx.NetworkXNoPath:
            pass
        # prev_u -> cur_v
        try:
            paths.append(get_fastest_path_with_time(rn, pre_u, cur_v, prev_candi_pt.offset / pre_speed,
                                                    (rn[cur_u][cur_v]['length'] - cur_candi_pt.offset) / cur_speed))
        except nx.NetworkXNoPath:
            pass
        # pre_v -> cur_u
        try:
            paths.append(get_fastest_path_with_time(rn, pre_v, cur_u, (rn[pre_u][pre_v]['length'] - prev_candi_pt.offset) / pre_speed,
                                                    cur_candi_pt.offset / cur_speed))
        except nx.NetworkXNoPath:
            pass
        # prev_v -> cur_v:
        try:
            paths.append(get_fastest_path_with_time(rn, pre_v, cur_v, (rn[pre_u][pre_v]['length'] - prev_candi_pt.offset) / pre_speed,
                                                    (rn[cur_u][cur_v]['length'] - cur_candi_pt.offset) / cur_speed))
        except nx.NetworkXNoPath:
            pass
        if len(paths) > 0:
            min_time, fastest_path = min(paths, key=lambda t: t[0])
        return min_time, fastest_path


def get_fastest_path_with_time(rn, src, dest, time_to_src, time_to_dest):
    time = 0.0
    path = nx.dijkstra_path(rn, src, dest, weight='time')
    time += time_to_src
    for i in range(len(path) - 1):
        start = path[i]
        end = path[i + 1]
        time += rn[start][end]['time']
    time += time_to_dest
    return time, path


def route_planning(rn, start_pt, end_pt):
    start_candidates = get_candidates(start_pt, rn, 300)
    end_candidates = get_candidates(end_pt, rn, 300)
    # no nearby edge to match error
    if start_candidates is None or end_candidates is None:
        return None
    start_candi_pt = min(start_candidates, key=lambda candi_pt: candi_pt.error)
    end_candi_pt = min(end_candidates, key=lambda candi_pt: candi_pt.error)
    min_time, fastest_path = find_fastest_path(rn, start_candi_pt, end_candi_pt)
    if min_time != float('inf'):
        # a path is found
        return construct_path(rn, start_pt, end_pt, start_candi_pt, end_candi_pt, fastest_path)
    # no pass found error
    else:
        return None


@app.route('/doPathPlan', methods=['GET'])
def route_planning_api():
    rn_name = request.args.get('tableName')
    if rn_name == 'majuqiao':
        rn = mjq_rn
    elif rn_name == 'fangshan':
        rn = fs_rn
    else:
        return jsonify({
            'success': False,
            'message': 'invalid table name'
        })
    start_point = request.args.get('startPoint')
    end_point = request.args.get('endPoint')
    start = start_point.split(',')
    start_pt = SPoint(float(start[1]), float(start[0]))
    end = end_point.split(',')
    end_pt = SPoint(float(end[1]), float(end[0]))
    results = route_planning(rn, start_pt, end_pt)
    if results is None:
        return jsonify({
            'success': False,
            'message': 'start or end point has no candidate to match, or no feasible route'
        })
    else:
        # all_coords = []
        # for result in results:
        #     all_coords.extend(result.coords)
        # return jsonify({
        #     'success': True,
        #     'data': [result.to_dict() for result in results],
        #     'wkt': to_wkt(all_coords)
        # })
        return jsonify({
            'success': True,
            'data': [result.to_dict() for result in results],
            'message': 'ok'
        })


def construct_path(rn, start_pt, end_pt, start_candi_pt, end_candi_pt, path):
    path_elements = []
    first_element = PathElement(-1, [start_pt, start_candi_pt], 1.0, distance(start_pt, start_candi_pt))
    path_elements.append(first_element)
    if len(path) == 0:
        edge = rn.edge_idx[start_candi_pt.eid]
        edge_coords = rn.edges[edge]['coords']
        coords = [start_candi_pt]
        # on the same road, doesn't cross any vertex
        # direction is the same with coords
        if start_candi_pt.offset < end_candi_pt.offset:
            offset = 0.0
            for i in range(len(edge_coords) - 1):
                offset += distance(edge_coords[i], edge_coords[i+1])
                if start_candi_pt.offset < offset < end_candi_pt.offset:
                    coords.append(edge_coords[i+1])
        # direction is opposite with coords
        else:
            offset = 0.0
            for i in range(len(edge_coords)-2, -1, -1):
                offset += distance(edge_coords[i+1], edge_coords[i])
                if end_candi_pt.offset < offset < start_candi_pt.offset:
                    coords.append(edge_coords[i])
        coords.append(end_candi_pt)
        mid_element = PathElement(rn.edges[edge]['eid'], coords, rn.edges[edge]['speed'], cal_dist(coords))
        path_elements.append(mid_element)
    else:
        first_vertex = path[0]
        first_edge = rn.edge_idx[start_candi_pt.eid]
        first_edge_coords = rn.edges[first_edge]['coords']
        first_edge_idx = get_edge_idx(first_edge_coords, start_candi_pt.offset)
        start_coords = construct_start_coords(start_candi_pt, first_edge_coords, first_edge_idx, first_vertex)
        path_elements.append(PathElement(start_candi_pt.eid, start_coords, rn.edges[first_edge]['speed'], cal_dist(start_coords)))
        for i in range(len(path) - 1):
            u, v = path[i], path[i+1]
            edge_coords = rn[u][v]['coords']
            mid_coords = construct_mid_coords(edge_coords, u)
            path_elements.append(PathElement(rn[u][v]['eid'], mid_coords, rn[u][v]['speed'], cal_dist(mid_coords)))
        last_vertex = path[-1]
        last_edge = rn.edge_idx[end_candi_pt.eid]
        last_edge_coords = rn.edges[last_edge]['coords']
        last_edge_idx = get_edge_idx(last_edge_coords, end_candi_pt.offset)
        end_coords = construct_end_coords(end_candi_pt, last_edge_coords, last_edge_idx, last_vertex)
        path_elements.append(PathElement(end_candi_pt.eid, end_coords, rn.edges[last_edge]['speed'], cal_dist(end_coords)))
    last_element = PathElement(-1, [end_candi_pt, end_pt], 1.0, distance(end_candi_pt, end_pt))
    path_elements.append(last_element)
    return path_elements


def construct_start_coords(start_candi_pt, first_edge_coords, first_edge_idx, first_vertex):
    first_vertex_pt = SPoint(first_vertex[1], first_vertex[0])
    traverse_coords = [start_candi_pt]
    # direction is same with coords
    if first_vertex_pt == first_edge_coords[-1]:
        for i in range(first_edge_idx+1, len(first_edge_coords)):
            traverse_coords.append(first_edge_coords[i])
    # direction is opposite with coords
    else:
        for i in range(first_edge_idx, -1, -1):
            traverse_coords.append(first_edge_coords[i])
    return traverse_coords


def construct_end_coords(end_candi_pt, last_edge_coords, last_edge_idx, last_vertex):
    last_vertex_pt = SPoint(last_vertex[1], last_vertex[0])
    traverse_coords = []
    # direction is same with coords
    if last_vertex_pt == last_edge_coords[0]:
        for i in range(0, last_edge_idx + 1):
            traverse_coords.append(last_edge_coords[i])
    # direction is opposite with coords
    else:
        for i in range(len(last_edge_coords)-1, last_edge_idx, -1):
            traverse_coords.append(last_edge_coords[i])
    traverse_coords.append(end_candi_pt)
    return traverse_coords


def construct_mid_coords(edge_coords, from_vertex):
    traverse_coords = []
    from_vertex_pt = SPoint(from_vertex[1], from_vertex[0])
    if edge_coords[0] == from_vertex_pt:
        for i in range(len(edge_coords)):
            traverse_coords.append(edge_coords[i])
    else:
        for i in range(len(edge_coords) - 1, -1, -1):
            traverse_coords.append(edge_coords[i])
    return traverse_coords


def get_edge_idx(edge_coords, offset):
    if offset == 0.0:
        return 0
    elif offset == cal_dist(edge_coords):
        return len(edge_coords) - 2
    else:
        edge_offset = 0.0
        edge_idx = 0
        for i in range(len(edge_coords) - 1):
            edge_offset += distance(edge_coords[i], edge_coords[i+1])
            if edge_offset > offset:
                edge_idx = i
                break
        return edge_idx


def cal_dist(coords):
    dist = 0.0
    for i in range(len(coords) - 1):
        dist += distance(coords[i], coords[i+1])
    return dist


def reachable_area_query_old(rn, start_pt, reachable_time):
    start_candidates = get_candidates(start_pt, rn, 50)
    # no nearby edge to match error
    if start_candidates is None:
        return None
    closed_set = set()
    edge_closed_set = set()
    pq = []
    edge_elements = []
    for start_candidate in start_candidates:
        u, v = rn.edge_idx[start_candidate.eid]
        edge_speed = rn[u][v]['speed']
        edge_elements.append(PathElement(start_candidate.eid, rn[u][v]['coords'], edge_speed, rn[u][v]['length']))
        edge_closed_set.add((u, v))
        edge_closed_set.add((v, u))
        dist_to_u = distance(start_candidate, SPoint(u[1], u[0]))
        cost_time_to_u = dist_to_u / edge_speed
        if cost_time_to_u < reachable_time:
            heappush(pq, (cost_time_to_u, u, (-1, -1)))
        dist_to_v = distance(start_candidate, SPoint(v[1], v[0]))
        cost_time_to_v = dist_to_v / edge_speed
        if cost_time_to_v < reachable_time:
            heappush(pq, (cost_time_to_v, v, (-1, -1)))
    while len(pq) > 0:
        cost_time, v, f = heappop(pq)
        if f != (-1, -1) and (v, f) not in edge_closed_set:
            edge_elements.append(PathElement(rn[v][f]['eid'], rn[v][f]['coords'], rn[v][f]['speed'], rn[v][f]['length']))
            edge_closed_set.add((v, f))
            edge_closed_set.add((f, v))
        closed_set.add(v)
        for x in rn.adj[v]:
            cost_time_to_x = cost_time + rn[x][v]['length'] / rn[x][v]['speed']
            if x not in closed_set and cost_time_to_x < reachable_time:
                heappush(pq, (cost_time_to_x, x, v))
    return edge_elements


def reachable_area_query(rn, start_pt, reachable_time):
    start_candidates = get_candidates(start_pt, rn, 300)
    # no nearby edge to match error
    if start_candidates is None:
        return None
    closed_set = set()
    edge_closed_set = set()

    edge_elements = []
    start_candidate = min(start_candidates, key=lambda candi_pt: candi_pt.error)
    # add the line to the nearest road
    first_element = PathElement(-1, [start_pt, start_candidate], 1.0, distance(start_pt, start_candidate))
    if first_element.time > reachable_time:
        return []
    edge_elements.append(first_element)
    pq = []
    u, v = rn.edge_idx[start_candidate.eid]
    edge_speed = rn[u][v]['speed']
    u_pt = SPoint(u[1], u[0])
    dist_to_u = distance(start_candidate, u_pt)
    cost_time_to_u = dist_to_u / edge_speed + first_element.time
    if cost_time_to_u < reachable_time:
        edge_elements.append(PathElement(start_candidate.eid, [start_candidate, u_pt], rn[u][v]['speed'],
                                         distance(start_candidate, u_pt)))
        heappush(pq, (cost_time_to_u, u, (-1, -1)))
        edge_closed_set.add((u, v))
        edge_closed_set.add((v, u))
    v_pt = SPoint(v[1], v[0])
    dist_to_v = distance(start_candidate, v_pt)
    cost_time_to_v = dist_to_v / edge_speed + first_element.time
    if cost_time_to_v < reachable_time:
        edge_elements.append(PathElement(start_candidate.eid, [start_candidate, v_pt], rn[u][v]['speed'],
                                         distance(start_candidate, v_pt)))
        heappush(pq, (cost_time_to_v, v, (-1, -1)))
        edge_closed_set.add((u, v))
        edge_closed_set.add((v, u))
    while len(pq) > 0:
        cost_time, v, f = heappop(pq)
        if f != (-1, -1) and (v, f) not in edge_closed_set:
            edge_elements.append(PathElement(rn[v][f]['eid'], rn[v][f]['coords'], rn[v][f]['speed'], rn[v][f]['length']))
            edge_closed_set.add((v, f))
            edge_closed_set.add((f, v))
        closed_set.add(v)
        for x in rn.adj[v]:
            cost_time_to_x = cost_time + rn[x][v]['length'] / rn[x][v]['speed']
            if x not in closed_set and cost_time_to_x < reachable_time:
                heappush(pq, (cost_time_to_x, x, v))
    return edge_elements


@app.route('/doSchedulingOptimization', methods=['GET'])
def reachable_area_query_api():
    rn_name = request.args.get('tableName')
    if rn_name == 'majuqiao':
        rn = mjq_rn
    elif rn_name == 'fangshan':
        rn = fs_rn
    else:
        return jsonify({
            'success': False,
            'message': 'invalid table name'
        })
    time_budget = float(request.args.get('expecttime')) * 60.0
    start_point = request.args.get('startPoint')
    start = start_point.split(',')
    start_pt = SPoint(float(start[1]), float(start[0]))
    results = reachable_area_query(rn, start_pt, time_budget)
    if results is None:
        return jsonify({
            'success': False,
            'message': 'start point has no candidate to match'
        })
    else:
        # return jsonify({
        #     'success': True,
        #     'data': [result.to_dict() for result in results],
        #     'wkt': [to_wkt(result.coords) for result in results]
        # })
        return jsonify({
            'success': True,
            'data': [result.to_dict() for result in results],
            'message': 'ok'
        })


def add_time_attribute(rn):
    for u, v, data in rn.edges(data=True):
        # avoid dividing by zero error
        # make sure every edge is traversable
        if data['speed'] == 0:
            data['speed'] = 1.0
        data['time'] = data['length'] / data['speed']



shapefile_path = 'beijing/edges.shp'  # 替换为你的 Shapefile 路径
graph_pickle_path = 'beijing/networkx_beijing.pkl'  # 图形数据的存储路径


load_rn_shp(shapefile_path)


if __name__ == '__main__':
    mjq_station_id = '1029'
    fs_station_id = '2120'
    base_dir = '../data/result/'
    # mjq_rn = load_rn_shp(base_dir + '{}/rn_{}_refined_s5_l10/'.format(mjq_station_id, mjq_station_id), is_directed=False)
    mjq_rn = load_rn_shp(base_dir + '{}/rn_{}_final/'.format(mjq_station_id, mjq_station_id), is_directed=False)
    add_time_attribute(mjq_rn)
    # fs_rn = load_rn_shp(base_dir + '{}/rn_{}_refined_s5_l10/'.format(fs_station_id, fs_station_id), is_directed=False)
    fs_rn = load_rn_shp(base_dir + '{}/rn_{}_final/'.format(fs_station_id, fs_station_id), is_directed=False)
    add_time_attribute(fs_rn)
    app.run(debug=True)
