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
    public class Services : WebServiceItem
    {
        [DataMemberAttribute]
        public int serviceID { get; set; }
        [DataMemberAttribute]
        public string serviceName { get; set; }
        [DataMemberAttribute]
        public string serviceWebURl { get; set; }
        [DataMemberAttribute]
        public string servicePictureURL { get; set; }
        [DataMemberAttribute]
        public string serviceIconURL { get; set; }
        [DataMemberAttribute]
        public string serviceDescription { get; set; }
        [DataMemberAttribute]
        public string servicePhone { get; set; }
        [DataMemberAttribute]
        public string serviceAddress { get; set; }
        [DataMemberAttribute]
        public double serviceLat { get; set; }
        [DataMemberAttribute]
        public double serviceLong { get; set; }
        [DataMemberAttribute]
        public string serviceCatIconURL { get; set; }
        [DataMemberAttribute]
        public string serviceCatName { get; set; } 

        protected override WebServiceItem objectFromDatasetRow(System.Data.DataRow dr)
        {
            Services services = new Services();
            services.serviceCatIconURL=Utils.ObjectToString(dr["serviceCatIconURL"]);
            services.serviceID = Utils.ObjectToInt(dr["serviceID"]);
            services.serviceName = Utils.ObjectToString(dr["serviceName"]);
            services.serviceWebURl = Utils.ObjectToString(dr["serviceWebURl"]);
            services.servicePictureURL = Utils.ObjectToString(dr["servicePictureURL"]);
            services.serviceIconURL = Utils.ObjectToString(dr["serviceIconURL"]);
            services.serviceDescription = Utils.ObjectToString(dr["serviceDescription"]);
            services.servicePhone = Utils.ObjectToString(dr["servicePhone"]);
            services.serviceAddress = Utils.ObjectToString(dr["serviceAddress"]);
            services.serviceLat = Utils.ObjectToDouble(dr["serviceLat"]);
            services.serviceLong = Utils.ObjectToDouble(dr["serviceLong"]);
            services.serviceCatName = Utils.ObjectToString(dr["serviceCatName"]);
            
            return services;
        }

        public List<Services> buildList()
        {
            List<Services> list = new List<Services>();
            foreach (DataRow dr in getDataSet().Tables[0].Rows)
            {
                list.Add((Services)objectFromDatasetRow(dr));
            }
            return list;
        }
    }
}