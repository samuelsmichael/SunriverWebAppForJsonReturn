using System;
using System.Collections;
using System.Configuration;
using System.Data;
using System.Linq;
using System.Web;
using System.Web.Security;
using System.Web.UI;
using System.Web.UI.HtmlControls;
using System.Web.UI.WebControls;
using System.Web.UI.WebControls.WebParts;
using System.Xml.Linq;
using System.Collections.Generic;
using System.IO;
using System.Text;
using System.Web.Script.Serialization;
using System.Runtime.Serialization.Json;
using System.Web.Services;

namespace SunriverWebApp {
    public partial class Map1 : System.Web.UI.Page {
        protected void Page_Load(object sender, EventArgs e) {
            MemoryStream ms = new MemoryStream();
            DataContractJsonSerializer ser = new DataContractJsonSerializer(typeof(List<Map>));
            ser.WriteObject(ms, new Map().buildList());
            ms.Flush();
            ms.Position = 0;
            System.IO.StreamReader sr = new StreamReader(ms);
            string str = sr.ReadToEnd();
            ms.Close();
            sr.Close();
            Response.Clear();
            Response.ContentType = "application/json; charset=utf-8";
            Response.Write(str);
            Response.End();
        }
        [WebMethod]
        public static List<Map> GetActivity() {
            return new Map().buildList();
        }
    }
}
