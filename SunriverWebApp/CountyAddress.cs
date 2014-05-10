using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Configuration;
using System.Data;
using System.Data.SqlClient;
using System.Runtime.Serialization;

namespace SunriverWebApp {
    [DataContractAttribute]
    public class CountyAddress {
        [DataMemberAttribute]
        public string mAddress { get; set; }
        public CountyAddress() {
            mAddress = String.Empty;
        }
        /*
         * An exception -> no item found
        */
        public CountyAddress(string resortName) {
            SqlConnection connection = null;
            SqlCommand command = null;
            try {
                String connectionString =
                    ConfigurationManager.ConnectionStrings["SROAddConvertConnectionString"].ConnectionString;
                    
                connection = new SqlConnection(connectionString);
                connection.Open();
                command=new SqlCommand("SELECT * FROM SRAddConvert WHERE SRAddress='"+normalizeResortName(resortName)+"'",connection);
                SqlDataAdapter adapter=new SqlDataAdapter(command);
                command.CommandType = CommandType.Text;
                DataSet ds=new DataSet();
                adapter.Fill(ds);
                mAddress =
                    ds.Tables[0].Rows[0]["DC_Address"] + " " +
                    ds.Tables[0].Rows[0]["SRCity"] + " " +
                    ds.Tables[0].Rows[0]["SRState"] + " " +
                    ds.Tables[0].Rows[0]["SRZip"];
            } finally {
                try { command.Dispose(); } catch { }
                try { connection.Close(); } catch { }
            }
        }
        private string normalizeResortName(string resortName) {
            return
                resortName
                    .ToLower()
                    .Replace("ln", "Lane");
        }
    }
}
