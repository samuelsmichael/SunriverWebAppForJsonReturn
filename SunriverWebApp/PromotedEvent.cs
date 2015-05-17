using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Data.Common;
using System.Data;
using System.Runtime.Serialization;
using System.Data.SqlClient;

namespace SunriverWebApp {
    public class PromotedEvent : WebServiceItem  {
        [DataMemberAttribute]
        public int promotedEventsID { get; set; }
        [DataMemberAttribute]
        public string promotedEventsName { get; set; }
        [DataMemberAttribute]
        public bool isOnPromotedEvents { get; set; }
        [DataMemberAttribute]
        public string promotedEventPictureURL { get; set; }
        [DataMemberAttribute]
        public int promotedCatID { get; set; }
        [DataMemberAttribute]
        public string promotedCatName { get; set; }
        [DataMemberAttribute]
        public int promotedCatSortOrder { get; set; }
        [DataMemberAttribute]
        public string promotedCatURLForIconImage { get; set; }
        [DataMemberAttribute]
        public int promotedEventsDetailsID { get; set; }
        [DataMemberAttribute]
        public string promotedEventsDetailsTitle { get; set; }
        [DataMemberAttribute]
        public string promotedEventsDetailsDescription { get; set; }
        [DataMemberAttribute]
        public string promotedEventsDetailsURLDocDownload { get; set; }
        [DataMemberAttribute]
        public string promotedEventsDetailsAddress { get; set; }
        [DataMemberAttribute]
        public string promotedEventsDetailsTelephone { get; set; }
        [DataMemberAttribute]
        public string promotedEventsDetailsWebsite { get; set; }
        [DataMemberAttribute]
        public string promotedEventIconURL { get; set; }
        [DataMemberAttribute]
        public string promotedEventsDetailIconURL { get; set; }
        [DataMemberAttribute]
        public int promotedEventDetailOrder { get; set; }

        protected override WebServiceItem objectFromDatasetRow(System.Data.DataRow dr) {
            PromotedEvent PromotedEvent = new PromotedEvent();
            PromotedEvent.promotedEventsID = Utils.ObjectToInt(dr["promotedEventsID"]);
            PromotedEvent.promotedEventsName = Utils.ObjectToString(dr["promotedEventsName"]);
            PromotedEvent.isOnPromotedEvents = Utils.ObjectToBool(dr["isOnPromotedEvents"]);
            PromotedEvent.promotedEventPictureURL = Utils.ObjectToString(dr["promotedEventPictureURL"]);
            PromotedEvent.promotedCatID = Utils.ObjectToInt(dr["promotedCatID"]);
            PromotedEvent.promotedCatName = Utils.ObjectToString(dr["promotedCatName"]);
            PromotedEvent.promotedCatSortOrder = Utils.ObjectToInt(dr["promotedCatSortOrder"]);
            PromotedEvent.promotedCatURLForIconImage = Utils.ObjectToString(dr["promotedCatURLForIconImage"]);
            PromotedEvent.promotedEventsDetailsID = Utils.ObjectToInt(dr["promotedEventsDetailsID"]);
            PromotedEvent.promotedEventsDetailsTitle = Utils.ObjectToString(dr["promotedEventsDetailsTitle"]);
            PromotedEvent.promotedEventsDetailsDescription = Utils.ObjectToString(dr["promotedEventsDetailsDescription"]);
            PromotedEvent.promotedEventsDetailsURLDocDownload = Utils.ObjectToString(dr["promotedEventsDetailsURLDocDownload"]);
            PromotedEvent.promotedEventsDetailsAddress = Utils.ObjectToString(dr["promotedEventsDetailsAddress"]);
            PromotedEvent.promotedEventsDetailsTelephone = Utils.ObjectToString(dr["promotedEventsDetailsTelephone"]);
            PromotedEvent.promotedEventsDetailsWebsite = Utils.ObjectToString(dr["promotedEventsDetailsWebsite"]);
            PromotedEvent.promotedEventIconURL = Utils.ObjectToString(dr["promotedEventIconURL"]);
            PromotedEvent.promotedEventsDetailIconURL = Utils.ObjectToString(dr["promotedEventDetailIconURL"]);
            PromotedEvent.promotedEventDetailOrder = Utils.ObjectToInt(dr["promotedEventOrderId"]);
            return PromotedEvent;
        }

        public List<PromotedEvent> buildList() {
            List<PromotedEvent> list = new List<PromotedEvent>();
            foreach (DataRow dr in getDataSet().Tables[0].Rows) {
                list.Add((PromotedEvent)objectFromDatasetRow(dr));
            }
            return list;
        }
        protected override System.Data.DataSet getDataSet() {
            SqlCommand cmd = new SqlCommand("uspPromotedEventsGet");
            return Utils.getDataSetFromStoredProcedure(cmd, ConnectionString);
        }
    }
}
