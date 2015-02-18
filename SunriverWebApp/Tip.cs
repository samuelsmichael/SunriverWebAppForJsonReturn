using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Data.Common;
using System.Data;
using System.Runtime.Serialization;

namespace SunriverWebApp {
    public class Tip : WebServiceItem  {
        [DataMemberAttribute]
        public int tipsID { get; set; }
        [DataMemberAttribute]
        public string tipsURL { get; set; }
        [DataMemberAttribute]
        public int tipsAndroidOrder { get; set; }
        [DataMemberAttribute]
        public int tipsAppleOrder { get; set; }

        protected override WebServiceItem objectFromDatasetRow(System.Data.DataRow dr) {
            Tip tip = new Tip();
            tip.tipsID = Utils.ObjectToInt(dr["tipsID"]);
            tip.tipsURL = Utils.ObjectToString(dr["tipsURL"]);
            tip.tipsAndroidOrder = Utils.ObjectToInt(dr["tipsAndroidOrder"]);
            tip.tipsAppleOrder = Utils.ObjectToInt(dr["tipsAppleOrder"]);
            return tip;
        }

        public List<Tip> buildList() {
            List<Tip> list = new List<Tip>();
            foreach (DataRow dr in getDataSet().Tables[0].Rows) {
                list.Add((Tip)objectFromDatasetRow(dr));
            }
            return list;

        }
    }
}
