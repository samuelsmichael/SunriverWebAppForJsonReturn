using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Data.SqlClient;
using System.Data.Common;
using System.Data;
using System.Runtime.Serialization;

namespace SunriverWebApp {
    [DataContractAttribute]
    public class Map : WebServiceItem {
        [DataMemberAttribute]
        public int srMapId { get; set; }
        [DataMemberAttribute]
        public string srMapName { get; set; }
        [DataMemberAttribute]
        public string srMapCategoryName { get; set; }
        [DataMemberAttribute]
        public int srMapCategory { get; set; }
        [DataMemberAttribute]
        public string srMapPhone { get; set; }
        [DataMemberAttribute]
        public string srMapDescription { get; set; }
        [DataMemberAttribute]
        public DateTime srMapDate { get; set; }
        [DataMemberAttribute]
        public string srMapDuration { get; set; }
        [DataMemberAttribute]
        public string srMapLinks { get; set; }
        [DataMemberAttribute]
        public string srMapUrlImage { get; set; }
        [DataMemberAttribute]
        public string srMapAddress { get; set; }
        [DataMemberAttribute]
        public double srMapLat { get; set; }
        [DataMemberAttribute]
        public double srMapLong { get; set; }
        [DataMemberAttribute]
        public bool isGPSpopup { get; set; }

        protected override WebServiceItem objectFromDatasetRow(System.Data.DataRow dr) {
            Map map = new Map();
            map.srMapId = Utils.ObjectToInt(dr["srMapId"]);
            map.srMapName = Utils.ObjectToString(dr["srMapName"]);
            map.srMapCategoryName = Utils.ObjectToString(dr["srMapCategoryName"]);
            map.srMapCategory = Utils.ObjectToInt(dr["srMapCategory"]);
            map.srMapPhone = Utils.ObjectToString(dr["srMapPhone"]);
            map.srMapDescription = Utils.ObjectToString(dr["srMapDescription"]);
            map.srMapDate = Utils.ObjectToDateTime(dr["srMapDate"]);
            map.srMapDuration = Utils.ObjectToString(dr["srMapDuration"]);
            map.srMapLinks = Utils.ObjectToString(dr["srMapLinks"]);
            map.srMapUrlImage = Utils.ObjectToString(dr["srMapUrlImage"]);
            map.srMapAddress = Utils.ObjectToString(dr["srMapAddress"]);
            map.srMapLat = Utils.ObjectToDouble(dr["srMapLat"]);
            map.srMapLinks = Utils.ObjectToString(dr["srMapLinks"]);
            map.srMapLong = Utils.ObjectToDouble(dr["srMapLong"]);
            map.isGPSpopup = Utils.ObjectToBool(dr["isGPSpopup"]);
            return map;
        }

        public List<Map> buildList() {
            List<Map> list = new List<Map>();
            foreach (DataRow dr in getDataSet().Tables[0].Rows) {
                list.Add((Map)objectFromDatasetRow(dr));
            }
            return list;
        }

    }
}
