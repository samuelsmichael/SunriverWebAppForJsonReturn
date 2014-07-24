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
        public static List<CountyAddress> FindCountyAddress(string resortName) {
            List<CountyAddress> returnList=new List<CountyAddress>();
            CountyAddress countyAddress = new CountyAddress();

            String[] sa = resortName.Split(new char[] { '~' });
            String number = sa[0];
            String lane = sa[1];
            String phoneId = sa[2];
            SqlConnection connection = null;
            SqlCommand command = null;
            try {
                String connectionString =
                    ConfigurationManager.ConnectionStrings["SROAddConvertConnectionString"].ConnectionString;

                connection = new SqlConnection(connectionString);
                connection.Open();
                command = new SqlCommand("SELECT * FROM SRAddConvert WHERE SRLot='" + number + "' AND SRLane='" + lane + "'", connection);
                SqlDataAdapter adapter = new SqlDataAdapter(command);
                command.CommandType = CommandType.Text;
                DataSet ds = new DataSet();
                adapter.Fill(ds);
                countyAddress.mAddress =
                    ds.Tables[0].Rows[0]["DC_Address"] + " " +
                    ds.Tables[0].Rows[0]["SRCity"] + " " +
                    ds.Tables[0].Rows[0]["SRState"] + " " +
                    ds.Tables[0].Rows[0]["SRZip"];
            }
            finally {
                try { command.Dispose(); }
                catch { }
                try { connection.Close(); }
                catch { }
                String connectionString2 =
                    ConfigurationManager.ConnectionStrings["SROFuzzyReturnsConnectionString"].ConnectionString;
                SqlConnection connection2=null;
                try {

                    connection2 = new SqlConnection(connectionString2);
                    connection2.Open();
                    command = new SqlCommand("INSERT INTO SRFuzzyReturns SELECT GETDATE(),'"+phoneId+"','"+number+" "+lane+"','"+countyAddress.mAddress+"'", connection2);
                    command.CommandType = CommandType.Text;
                    command.ExecuteNonQuery();
                } catch (Exception eE) {
                    countyAddress.mAddress=eE.ToString()+"||"+connectionString2 + "||"+command.CommandText;
                }
                finally {
                    try { command.Dispose(); }
                    catch { }
                    try { connection2.Close(); }
                    catch { }
                } 
            }

            returnList.Add(countyAddress);
            return returnList;
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
