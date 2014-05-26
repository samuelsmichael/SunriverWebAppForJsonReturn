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
    public class Activity : WebServiceItem {
        [DataMemberAttribute]
        public int srActID { get; set; }
        [DataMemberAttribute]
        public string srActName { get; set; }
        [DataMemberAttribute]
        public string srActDescription { get; set; }
        [DataMemberAttribute]
        public DateTime? srActDate { get; set; }
        [DataMemberAttribute]
        public string srActTime { get; set; }
        [DataMemberAttribute]
        public string srActDuration { get; set; }
        [DataMemberAttribute]
        public string srActLinks { get; set; }
        [DataMemberAttribute]
        public string srActUrlImage { get; set; }
        [DataMemberAttribute]
        public string srActAddress { get; set; }
        [DataMemberAttribute]
        public double srActLat { get; set; }
        [DataMemberAttribute]
        public double srActLong { get; set; }
        [DataMemberAttribute]
        public bool isApproved { get; set; }

        protected override WebServiceItem objectFromDatasetRow(System.Data.DataRow dr) {
            Activity activity = new Activity();
            activity.srActID = Utils.ObjectToInt(dr["srActID"]);
            activity.srActName = Utils.ObjectToString(dr["srActName"]);
            activity.srActDescription = Utils.ObjectToString(dr["srActDescription"]);
            activity.srActDate = Utils.ObjectToDateTimeNullable(dr["srActDate"]);
            activity.srActTime = Utils.ObjectToString(dr["srActTime"]);
            activity.srActDuration = Utils.ObjectToString(dr["srActDuration"]);
            activity.srActUrlImage = Utils.ObjectToString(dr["srActUrlImage"]);
            activity.srActAddress = Utils.ObjectToString(dr["srActAddress"]);
            activity.srActLat = Utils.ObjectToDouble(dr["srActLat"]);
            activity.srActLinks = Utils.ObjectToString(dr["srActLinks"]);
            activity.srActLong = Utils.ObjectToDouble(dr["srActLong"]);
            activity.isApproved=Utils.ObjectToBool(dr["isApproved"]);
            return activity;
        }
        
        public List<Activity> buildList() {
            List<Activity> list = new List<Activity>();
            foreach (DataRow dr in getDataSet().Tables[0].Rows) {
                list.Add((Activity)objectFromDatasetRow(dr));
            }
            return list;
        }
    }
}
