import sqlite3
import traceback
import os
import sys
import datetime
from datetime import timedelta
from collections import defaultdict
from math import sin, cos, sqrt, atan2, radians



def getOpenConnection(user='postgres', password='54321', dbname='postgres'):
    return sqlite3.connect("data/"+dbname)
dateFormat = "yyyyMMddHHmmssDAY"
class userLocation:
    id = ""
    time = None
    latitude = 0
    longitude = 0
    visited = bool(0)

    def __init__(self, id, time, lat, lon):
        self.id = id
        self.time =datetime.datetime.strptime(time,'%Y%m%d')
        self.latitude = lat
        self.longitude = lon

def M(adj):
    mat = []
    for i in range(0,12):
        line = []
        coll =[]
        if(len(adj["GS"+(str(i+1))])>0):
            for node in adj["GS"+(str(i+1))][0]:
                coll.append(node[0])
            for j in range(0,12):
                if("GS"+str(j+1) in coll):
                    line.append(1)
                else:
                    line.append(0)
        else:
            for j in range(0,12):
                line.append(0)
        mat.append(line)

    return mat

#def getMatrix(adjMat):
#    adj = []
#    adj = np.array(adj)
#    for id in adjMat.keys():
#        for cont in adjMat[id][0]:
#            adj[idOf(id)-1][idOf(cont[0])-1] = 1
#            adj[idOf(cont[0])-1][idOf(id)-1] = 1
#    return adj

def idOf(uid):
    return int(uid.replace("GS",""))

def getDistance(latA,lonA,latB, lonB):
    R = 6373.0

    lat1 = radians(latA)
    lon1 = radians(lonA)
    lat2 = radians(latB)
    lon2 = radians(lonB)

    dlon = lon2 - lon1
    dlat = lat2 - lat1

    a = sin(dlat / 2)**2 + cos(lat1) * cos(lat2) * sin(dlon / 2)**2
    c = 2 * atan2(sqrt(a), sqrt(1 - a))

    distance = R * c
    return distance

def getData(date, ofPastnDays):
    path = 'data/'
    #pastDate = datetime.datetime.strptime(date,'%Y%m%d') - timedelta(days=7)
    files = []
    userData = defaultdict(list)
    for i in os.listdir(path):
        if(i.startswith("LifeMap_")):
            files.append(i)
    for db in files:
        for i in range(ofPastnDays,0,-1):
            pastdate = datetime.datetime.strptime(date,'%Y%m%d') - timedelta(days = i)
            clause = pastdate.strftime('%Y%m%d')
            conn = getOpenConnection(dbname = db)
            cur = conn.cursor()
            query = "SELECT _node_id, _time_location, _latitude, _longitude FROM locationTable WHERE _time_location LIKE '%s' ORDER BY _node_id"%(clause + '%')
            cur.execute(query)
            results = cur.fetchall()
            dayLocData = []
            for row in results:
                key = db.split(".")[0].replace("LifeMap_","")
                val = 1/1000000
                if(row[2]>0 and row[3]>0):
                    dayLocData.append(userLocation(row[0],row[1][:-9],row[2]*val,row[3]*val))
                else:
                    continue
            if(len(results)>0):
                userData[key].append(dayLocData)
    return userData

def compareTwo(data1, data2, threshold):
    if(data1.latitude<=0 and data1.longitude<=0 and data2.latitude<=0 and data2.longitude):
        return False
    d = getDistance(data1.latitude,data1.longitude,data2.latitude,data2.longitude)
    if(d<=int(threshold)):
        #print(str(data1.id) + "->" + str(d) +"<-" + str(data2.id))
        return True
            


def getAdjMatrix(userData, id, days, startdate, mat):
    contacts = []
    #contact[id] = None
    found = False
    try:
        for i in range(startdate,len(userData[id])):
            if(len(userData[id])>0):
                for locData in userData[id][i]:
                    count = 0
                    for key in userData.keys():
                        count += 1
                        if(key == id):
                            continue
                        if((key in mat[id])):
                            continue
                        if(key in map(lambda x:x[0],contacts)):
                            continue
                        for loc in userData[key][i]:
                            if(compareTwo(locData,loc,1)):
                                #print(key)
                                prox = [key,i]
                                contacts.append(prox)
                                found = True
                                break
                        if(found):
                            break
                #    if(len(contacts) == len(userData.keys())-1):
                #        break
                #if(len(contacts) == len(userData.keys())-1):
                #    break
        mat[id].append(contacts)
        for con in contacts:
            if(con[0] in mat.keys()):
                continue
            getAdjMatrix(userData, con[0], 7, con[1], mat )
        return
    except Exception as detail:
            traceback.print_exc()



            
                    

if __name__ == '__main__':
    try:
        #adj = []
        userData = {}
        date = sys.argv[2]
        userData = getData(date,7)
        id = "GS"+ str(sys.argv[1])
        mat = defaultdict(list)
        mat[id] = []
        getAdjMatrix(userData, id, 7, 0,mat)
        matrix = M(mat)
        file= open("matrix.txt","w+")
        ln = []
        for line in matrix:
            for i in line:
                ln.append(str(i) + " ")
            ln.append("\n")
        strin = ""
        for ch in ln:
            strin += ch
        file.write(strin)
        file.close()
    except Exception as e:
        traceback.print_exc()