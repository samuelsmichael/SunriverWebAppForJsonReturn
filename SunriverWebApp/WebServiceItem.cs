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
            string query = "Select * from " + ConfigurationManager.AppSettings[GetType().Name + "TableName"] + (getIncludeOnlyWhereIsApprovedEqual1()?" WHERE isApproved=1":"");
            return Utils.getDataSetFromQuery(query, ConnectionString);
        }
        private bool getIncludeOnlyWhereIsApprovedEqual1() {
            return Utils.ObjectToBool(ConfigurationManager.AppSettings[GetType().Name + "IncludeOnlyWhereIsApprovedEqual1"]);
        }
    }
}
