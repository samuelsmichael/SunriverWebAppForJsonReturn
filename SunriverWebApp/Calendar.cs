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
    public class Calendar : WebServiceItem {
        [DataMemberAttribute]
        public int srCalId { get; set; }
        [DataMemberAttribute]
        public string srCalName { get; set; }
        [DataMemberAttribute]
        public string srCalDescription { get; set; }
        [DataMemberAttribute]
        public DateTime srCalDate { get; set; }
        [DataMemberAttribute]
        public string srCalTime { get; set; }
        [DataMemberAttribute]
        public string srCalDuration { get; set; }
        [DataMemberAttribute]
        public string srCalLinks { get; set; }
        [DataMemberAttribute]
        public string srCalUrlImage { get; set; }
        [DataMemberAttribute]
        public string srCalAddress { get; set; }
        [DataMemberAttribute]
        public double srCalLat { get; set; }
        [DataMemberAttribute]
        public double srCalLong { get; set; }
        [DataMemberAttribute]
        public bool isApproved { get; set; }

        protected override WebServiceItem objectFromDatasetRow(System.Data.DataRow dr) {
            Calendar calendar = new Calendar();
            calendar.srCalId = Utils.ObjectToInt(dr["srCalID"]);
            calendar.srCalAddress = Utils.ObjectToString(dr["srCalAddress"]);
            calendar.srCalDate = Utils.ObjectToDateTime(dr["srCalDate"]);
            calendar.srCalDuration = Utils.ObjectToString(dr["srCalDuration"]);
            calendar.srCalLat = Utils.ObjectToDouble(dr["srCalLat"]);
            calendar.srCalDescription = Utils.ObjectToString(dr["srCalDescription"]);
            calendar.srCalLinks = Utils.ObjectToString(dr["srCalLinks"]);
            calendar.srCalName = Utils.ObjectToString(dr["srCalName"]);
            calendar.srCalLong = Utils.ObjectToDouble(dr["srCalLong"]);
            calendar.srCalTime = Utils.ObjectToString(dr["srCalTime"]);
            calendar.srCalUrlImage = Utils.ObjectToString(dr["srCalUrlImage"]);
            calendar.isApproved=Utils.ObjectToBool(dr["isApproved"]);
            return calendar;
        }

        public List<Calendar> buildList() {
            List<Calendar> list = new List<Calendar>();
            foreach (DataRow dr in getDataSet().Tables[0].Rows) {
                list.Add((Calendar)objectFromDatasetRow(dr));
            }
            return list;
        }  
    }
}
