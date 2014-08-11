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
    public class EmergencyMaps : WebServiceItem
    {
        [DataMemberAttribute]
        public int emergencyMapsId { get; set; }
        [DataMemberAttribute]
        public string emergencyMapsURL { get; set; }
        [DataMemberAttribute]
        public string emergencyMapsDescription { get; set; }
        [DataMemberAttribute]
        public string emergencyMapsPic { get; set; }
        [DataMemberAttribute]
        public bool emergencyMapsIsVisible { get; set; }

        protected override WebServiceItem objectFromDatasetRow(System.Data.DataRow dr)
        {
            EmergencyMaps emergencyMaps = new EmergencyMaps();
            emergencyMaps.emergencyMapsId = Utils.ObjectToInt(dr["emergencyMapsId"]);
            emergencyMaps.emergencyMapsURL = Utils.ObjectToString(dr["emergencyMapsURL"]);
            emergencyMaps.emergencyMapsDescription = Utils.ObjectToString(dr["emergencyMapsDesciption"]);
            emergencyMaps.emergencyMapsPic = Utils.ObjectToString(dr["emergencyMapsPic"]);
            emergencyMaps.emergencyMapsIsVisible = Utils.ObjectToBool(dr["emergencyMapsIsVisible"]);
            return emergencyMaps;
        }

        public List<EmergencyMaps> buildList()
        {
            List<EmergencyMaps> list = new List<EmergencyMaps>();
            foreach (DataRow dr in getDataSet().Tables[0].Rows)
            {
                list.Add((EmergencyMaps)objectFromDatasetRow(dr));
            }
            return list;
        }
    }
}