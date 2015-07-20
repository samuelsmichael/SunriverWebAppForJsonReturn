using System;
using System.Collections.Generic;
using System.Linq;
using System.Configuration;
using System.Web;
using System.Data.Common;
using System.Data;
using System.Runtime.Serialization;

namespace SunriverWebApp
{
    [DataContractAttribute]
    public class Alert : WebServiceItem
    {
        [DataMemberAttribute]
        public int ALID { get; set; }
        [DataMemberAttribute]
        public string ALTitle { get; set; }
        [DataMemberAttribute]
        public string ALDescription { get; set; }
        [DataMemberAttribute]
        public bool isOnAlert { get; set; }

        protected override WebServiceItem objectFromDatasetRow(System.Data.DataRow dr)
        {
            Alert alert = new Alert();
            alert.ALID = Utils.ObjectToInt(dr["ALID"]);
            alert.ALTitle = Utils.ObjectToString(dr["ALTitle"]);
            alert.ALDescription = Utils.ObjectToString(dr["ALDescription"]);
            alert.isOnAlert = Utils.ObjectToBool(dr["isOnAlert"]);
            return alert;
        }

        protected override System.Data.DataSet getDataSet() {
            String query = "Select * from " + ConfigurationManager.AppSettings[GetType().Name + "TableName"] +  " WHERE isOnAlert=1";
            DataSet marre = Utils.getDataSetFromQuery(query, ConnectionString);
            return marre;
        }


        public List<Alert> buildList()
        {
            List<Alert> list = new List<Alert>();
            foreach (DataRow dr in getDataSet().Tables[0].Rows)
            {
                list.Add((Alert)objectFromDatasetRow(dr));
            }
            return list;

        }

    }
}