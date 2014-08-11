using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Data.SqlClient;
using System.Data.Common;
using System.Data;
using System.Runtime.Serialization;

namespace SunriverWebApp
{

    [DataContractAttribute]
    public class Emergency : WebServiceItem
    {
        [DataMemberAttribute]
        public int emergencyId { get; set; }
        [DataMemberAttribute]
        public string emergencyTitle { get; set; }
        [DataMemberAttribute]
        public string emergencyDescription { get; set; }
        [DataMemberAttribute]
        public bool isEmergencyAlert { get; set; }
        [DataMemberAttribute]
        public bool hasMap { get; set; }

        protected override WebServiceItem objectFromDatasetRow(System.Data.DataRow dr)
        {
            Emergency emergency = new Emergency();
            emergency.emergencyId = Utils.ObjectToInt(dr["emergencyId"]);
            emergency.emergencyTitle = Utils.ObjectToString(dr["emergencyTitle"]);
            emergency.emergencyDescription = Utils.ObjectToString(dr["emergencyDescription"]);
            emergency.isEmergencyAlert = Utils.ObjectToBool(dr["isEmergencyAlert"]);
            emergency.hasMap = Utils.ObjectToBool(dr["hasMap"]);
            return emergency;
        }

        public List<Emergency> buildList()
        {
            List<Emergency> list = new List<Emergency>();
            foreach (DataRow dr in getDataSet().Tables[0].Rows)
            {
                list.Add((Emergency)objectFromDatasetRow(dr));
            }
            return list;
        }
    }
}