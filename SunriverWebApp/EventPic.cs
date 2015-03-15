using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Data.Common;
using System.Data;
using System.Runtime.Serialization;
using System.Data.SqlClient;

namespace SunriverWebApp {
    public class EventPic : WebServiceItem  {
        [DataMemberAttribute]
        public int eventPicsID { get; set; }
        [DataMemberAttribute]
        public string eventPicsURL { get; set; }

        protected override WebServiceItem objectFromDatasetRow(System.Data.DataRow dr) {
            EventPic EventPic = new EventPic();
            EventPic.eventPicsID = Utils.ObjectToInt(dr["eventPicsID"]);
            EventPic.eventPicsURL = Utils.ObjectToString(dr["eventPicsURL"]);
            return EventPic;
        }

        public List<EventPic> buildList() {
            List<EventPic> list = new List<EventPic>();
            foreach (DataRow dr in getDataSet().Tables[0].Rows) {
                list.Add((EventPic)objectFromDatasetRow(dr));
            }
            return list;
        }
    }
}
