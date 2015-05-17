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
using System.Web.Script.Serialization;
using System.Runtime.Serialization.Json;
using System.Web.Services;
using System.Collections.Generic;
using System.IO;
using Newtonsoft.Json;
using System.Text;

namespace SunriverWebApp {
    public partial class Update1 : System.Web.UI.Page {
        public static HttpResponse bubba;
        protected void Page_Load(object sender, EventArgs e) {
            bubba = Response;
            //Response.Write("Hi Jason!  I made it successfully to Update.aspx.cs at line 24...PRE-anything!<br>");
            // successfully got here Response.End();
            MemoryStream ms = new MemoryStream();
            JsonSerializer serializer = new Newtonsoft.Json.JsonSerializer();
            using (JsonTextWriter jsonTextWriter = new JsonTextWriter(
                new StreamWriter(ms, new UTF8Encoding(false, true))) { CloseOutput = false }) {
                serializer.Serialize(jsonTextWriter, new Update().buildList());
                jsonTextWriter.Flush();
               // Response.Write("Hi Jason!  I made it successfully to Update.aspx.cs at line 32...POST-jsonSerialzieStep2<br>");
            }
            //Response.Write("Hi Jason!  I made it successfully to Update.aspx.cs at line 32...PRE-jsonSerialzieStep2<br>");
            Utils.jsonSerializeStep2(ms, Response);
        }
    }
}
