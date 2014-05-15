﻿using System;
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
        protected void Page_Load(object sender, EventArgs e) {
            JsonSerializer serializer = new Newtonsoft.Json.JsonSerializer();
            MemoryStream ms = new MemoryStream();
            using (JsonTextWriter jsonTextWriter = new JsonTextWriter(
                new StreamWriter(ms, new UTF8Encoding(false, true))) { CloseOutput = false })
               {
                   serializer.Serialize(jsonTextWriter, new Update().buildList());
                   jsonTextWriter.Flush();
              }


//            DataContractJsonSerializer ser = new DataContractJsonSerializer(typeof(List<Update>));
  //          ser.WriteObject(ms, new Update().buildList());
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
        public static List<Update> Update() {
            return new Update().buildList();
        }
    }
}