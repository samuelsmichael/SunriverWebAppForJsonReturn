using System;
using System.Data;
using System.Configuration;
using System.Linq;
using System.Web;
using System.Web.Security;
using System.Web.UI;
using System.Web.UI.HtmlControls;
using System.Web.UI.WebControls;
using System.Web.UI.WebControls.WebParts;
using System.Xml.Linq;
using System.Collections.Generic;

namespace SunriverWebApp {
    [System.Runtime.Serialization.DataContractAttribute]
    public class DidYouKnow : WebServiceItem {
        [System.Runtime.Serialization.DataMemberAttribute]
        public int didYouKnowId { get; set; }
        [System.Runtime.Serialization.DataMemberAttribute]
        public String didYouKnowURL { get; set; }
        protected override WebServiceItem objectFromDatasetRow(System.Data.DataRow dr) {
            DidYouKnow didYouKnow = new DidYouKnow();
            didYouKnow.didYouKnowId = Utils.ObjectToInt(dr["didYouKnowId"]);
            didYouKnow.didYouKnowURL = Utils.ObjectToString(dr["didYouKnowURL"]);
            return didYouKnow;
        }
        public List<DidYouKnow> buildList() {
            List<DidYouKnow> list = new List<DidYouKnow>();
            foreach (DataRow dr in getDataSet().Tables[0].Rows) {
                list.Add((DidYouKnow)objectFromDatasetRow(dr));
            }
            return list;
        }

    }
}
