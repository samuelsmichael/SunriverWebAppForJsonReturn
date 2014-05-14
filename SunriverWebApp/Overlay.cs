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
    public class Overlay : WebServiceItem {
        [System.Runtime.Serialization.DataMemberAttribute]
        public int overlayId {get;set;}
        [System.Runtime.Serialization.DataMemberAttribute]
        public string overlayLsURL {get;set;}
        [System.Runtime.Serialization.DataMemberAttribute]
        public string overlayLsSelectURL {get; set;}
        [System.Runtime.Serialization.DataMemberAttribute]
        public string overlayPortURL {get; set;}
        [System.Runtime.Serialization.DataMemberAttribute]
        public string overlayPortCamURL { get; set; }
        protected override WebServiceItem objectFromDatasetRow(System.Data.DataRow dr) {
            Overlay overlay = new Overlay();
            overlay.overlayId = Utils.ObjectToInt(dr["overlayId"]);
            overlay.overlayLsSelectURL = Utils.ObjectToString(dr["overlayLsSelectURL"]);
            overlay.overlayLsURL = Utils.ObjectToString(dr["overlayLsURL"]);
            overlay.overlayPortCamURL = Utils.ObjectToString(dr["overlayPortCamURL"]);
            overlay.overlayPortURL = Utils.ObjectToString(dr["overlayPortURL"]);
            return overlay;
        }
        public List<Overlay> buildList() {
            List<Overlay> list = new List<Overlay>();
            foreach (DataRow dr in getDataSet().Tables[0].Rows) {
                list.Add((Overlay)objectFromDatasetRow(dr));
            }
            return list;
        }
    }
}
