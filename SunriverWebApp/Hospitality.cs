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
    public class Hospitality : WebServiceItem {
        [DataMemberAttribute]
        public int srHospitalityID { get; set; }
        [DataMemberAttribute]
        public string srHospitalityName { get; set; }
        [DataMemberAttribute]
        public string srHospitalityDescription { get; set; }
        [DataMemberAttribute]
        public string srHospitalityUrlWebsite { get; set; }
        [DataMemberAttribute]
        public string srHospitalityUrlImage { get; set; }
        [DataMemberAttribute]
        public string srHospitalityAddress { get; set; }
        [DataMemberAttribute]
        public double srHospitalityLat { get; set; }
        [DataMemberAttribute]
        public double srHospitalityLong { get; set; }
        [DataMemberAttribute]
        public bool srHospitalityIsApproved { get; set; }
        [DataMemberAttribute]
        public string srHospitalityPhone {get; set;}

        protected override WebServiceItem objectFromDatasetRow(System.Data.DataRow dr) {
            Hospitality hospitality = new Hospitality();
            hospitality.srHospitalityID = Utils.ObjectToInt(dr["srHospitalityID"]);
            hospitality.srHospitalityName = Utils.ObjectToString(dr["srHospitalityName"]);
            hospitality.srHospitalityDescription = Utils.ObjectToString(dr["srHospitalityDescription"]);
            hospitality.srHospitalityUrlWebsite = Utils.ObjectToString(dr["srHospitalityUrlWebsite"]);
            hospitality.srHospitalityUrlImage = Utils.ObjectToString(dr["srHospitalityUrlImage"]);
            hospitality.srHospitalityAddress = Utils.ObjectToString(dr["srHospitalityAddress"]);
            hospitality.srHospitalityLat = Utils.ObjectToDouble(dr["srHospitalityLat"]);
            hospitality.srHospitalityPhone = Utils.ObjectToString(dr["srHospitalityPhone"]);
            hospitality.srHospitalityLong = Utils.ObjectToDouble(dr["srHospitalityLong"]);
            hospitality.srHospitalityIsApproved=Utils.ObjectToBool(dr["srHospitalityIsApproved"]);
            return hospitality;
        }

        public static List<Hospitality> Sample {
            get {
                List<Hospitality> list = new List<Hospitality>();
                list.Add(new Hospitality {
                    srHospitalityAddress = "18575 SW Century Dr",
                    srHospitalityDescription = "Seventh Mountain Resort is your winter and summer resort destination in sunny Central Oregon. Just a short drive up Century Drive, and just past Widgi Creek, one of the area’s finest golf courses, you’ll find the closest lodging to Mt. Bachelor.",
                    srHospitalityID=1,
                    srHospitalityIsApproved=true,
                    srHospitalityLat = 43.996642,
                    srHospitalityLong = -121.395916,
                    srHospitalityName="Seventh Mountain Resort",
                    srHospitalityPhone = "(541) 382-8711",
                    srHospitalityUrlImage = "http://seventhmountain.com/images/employment-img-summer.jpg",
                    srHospitalityUrlWebsite = "http://www.seventhmountain.com/"
                });
                list.Add(new Hospitality {
                    srHospitalityAddress = "17600 Center Dr",
                    srHospitalityDescription = "Sunriver Resort, near Bend, Oregon, offers lodging options and vacation rentals and  brings the beauty of the natural world and premier hotel accommodations together in the foothills of the Cascade Mountains. Year-round outdoor adventure and recreational activities are abundant at Sunriver; including world-class golf, skiing, kayaking, and more.",
                    srHospitalityID = 1,
                    srHospitalityIsApproved = true,
                    srHospitalityLat = 43.874205,
                    srHospitalityLong = -121.445904,
                    srHospitalityName = "The Sunriver Resort",
                    srHospitalityPhone = "1-800-801-8765",
                    srHospitalityUrlImage = "http://www.sunriver-resort.com/images/masthead/javabanner_lodgerear_welcome.jpg",
                    srHospitalityUrlWebsite = "http://www.sunriver-resort.com/"
                });
                return list;
            }
        }

        public List<Hospitality> buildList() {
            List<Hospitality> list = new List<Hospitality>();
            foreach (DataRow dr in getDataSet().Tables[0].Rows) {
                list.Add((Hospitality)objectFromDatasetRow(dr));
            }
            return list;
        }
    }
}
