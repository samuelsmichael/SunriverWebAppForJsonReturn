using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Data.Common;
using System.Data;
using System.Runtime.Serialization;
using System.Data.SqlClient;

namespace SunriverWebApp {
    public class NewsFeed : WebServiceItem  {
        [DataMemberAttribute]
        public int newsFeedID { get; set; }
        [DataMemberAttribute]
        public string newsFeedTitle { get; set; }
        [DataMemberAttribute]
        public string newsFeedDescription { get; set; }
        [DataMemberAttribute]
        public bool isOnNewsFeedAlert { get; set; }
        protected override WebServiceItem objectFromDatasetRow(System.Data.DataRow dr) {
            NewsFeed NewsFeed = new NewsFeed();
            NewsFeed.newsFeedID = Utils.ObjectToInt(dr["newsFeedID"]);
            NewsFeed.newsFeedTitle = Utils.ObjectToString(dr["newsFeedTitle"]);
            NewsFeed.isOnNewsFeedAlert = Utils.ObjectToBool(dr["isOnNewsFeedAlert"]);
            NewsFeed.newsFeedDescription = Utils.ObjectToString(dr["newsFeedDescription"]);
            return NewsFeed;
        }

        public List<NewsFeed> buildList() {
            List<NewsFeed> list = new List<NewsFeed>();
            foreach (DataRow dr in getDataSet().Tables[0].Rows) {
                list.Add((NewsFeed)objectFromDatasetRow(dr));
            }
            return list;
        }
    }
}
