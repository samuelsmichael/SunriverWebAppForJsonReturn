using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Configuration;
using System.Data;

namespace SunriverWebApp {
    [Serializable]
    public abstract class WebServiceItem {
        protected abstract WebServiceItem objectFromDatasetRow(DataRow dr);

        protected string ConnectionString {
            get {
                ConnectionStringSettings settings =
                    ConfigurationManager.ConnectionStrings[ConfigurationManager.AppSettings[GetType().Name + "ConnectionString"]];
                return settings.ConnectionString;
            }
        }

        protected virtual System.Data.DataSet getDataSet() {
            String query = "Select * from " + ConfigurationManager.AppSettings[GetType().Name + "TableName"] + (getIncludeOnlyWhereIsApprovedEqual1()?" WHERE isApproved=1":"");

            //Update1.bubba.Write("Hi Jason!  Here's the query string: " + query + "<br>and here's the ConnectionString" + ConnectionString);


            DataSet marre = Utils.getDataSetFromQuery(query, ConnectionString);
           // Update1.bubba.Write("<br>Hi Jason! The query is "+ (marre==null?"Null":("not NULL<br>")));
           // Update1.bubba.End();
            //Update1.bubba.Write("Hi Jason!  DataSet tables count after return: " + 
             //   (marre.Tables==null?"0":marre.Tables.Count+"<br>"));
            //Update1.bubba.End();
            return marre;
        }
        private bool getIncludeOnlyWhereIsApprovedEqual1() {
            return Utils.ObjectToBool(ConfigurationManager.AppSettings[GetType().Name + "IncludeOnlyWhereIsApprovedEqual1"]);
        }
    }
}
