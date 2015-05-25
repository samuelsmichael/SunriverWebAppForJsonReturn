using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Data.SqlClient;
using System.Data.Common;
using System.Data;
using System.Runtime.Serialization;
using System.Configuration;

namespace SunriverWebApp {
    [DataContractAttribute]
    public class SunriverLane : WebServiceItem {
        [DataMemberAttribute]
        public string srLaneName { get; set; }

        protected override WebServiceItem objectFromDatasetRow(System.Data.DataRow dr) {
            SunriverLane sunriverLane = new SunriverLane();
            sunriverLane.srLaneName = Utils.ObjectToString(dr["SRLane"]);
            return sunriverLane;
        }
        
        public List<SunriverLane> buildList() {
            List<SunriverLane> list = new List<SunriverLane>();
            foreach (DataRow dr in getDataSet().Tables[0].Rows) {
                list.Add((SunriverLane)objectFromDatasetRow(dr));
            }
            return list;
        }
        protected override DataSet getDataSet() {
            String query = "SELECT DISTINCT SRLane FROM SRAddConvert ORDER BY SRLane";
            return Utils.getDataSetFromQuery(query, ConfigurationManager.ConnectionStrings["SROAddConvertConnectionString"].ConnectionString);
        }
    }
}
