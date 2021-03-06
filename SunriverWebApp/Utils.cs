﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Data;
using System.Data.Common;
using System.Data.SqlClient;
using System.Text;
using System.IO;


namespace SunriverWebApp {
    public class Utils {
        public static void jsonSerializeStep2(MemoryStream ms, HttpResponse Response) {
            ms.Flush();
            ms.Position = 0;
            System.IO.StreamReader sr = new StreamReader(ms);
            string str = sr.ReadToEnd();

            //Response.Write("Hi Jason!  I made it successfully to Utils.cs ... POST-sr.ReadToEnd();<br>");
           // Response.Write("<div ><b>It's not publishing my site!!!</b></div>"); 
            //Response.End();
            //Response.Write("Here's the string that's about ready to get passed back:<br>"+str);
           // Response.End();

            ms.Close();
            sr.Close();
            Response.Clear();
            Response.ContentType = "application/json; charset=utf-8";
            Response.Write(str);
            Response.End();
        }

        public enum PAD_DIRECTION {
            LEFT, RIGHT
        }
        public static int NULL_INT = 40404040;
        public static decimal NULL_DECIMAL = 40404040m;
        public static DateTime NULL_DATETIME = DateTime.MinValue;
        public static DateTime SQL_NULL_DATETIME = new DateTime(1900, 1, 1);

        /// <summary>
        /// Retrieves a DataSet from a cmd whose cmd.Text is a Stored Procedure
        /// </summary>
        /// <param name="cmd"></param>
        /// <param name="connectionString"></param>
        /// <returns></returns>
        public static DataSet getDataSetFromStoredProcedure(SqlCommand cmd, string connectionString) {
            SqlConnection connection = null;
            try {
                connection = new SqlConnection(connectionString);
                connection.Open();
                cmd.Connection = connection;
                cmd.CommandType = CommandType.StoredProcedure;
                DataSet ds = new DataSet();
                DataAdapter da = new SqlDataAdapter(cmd);
                da.Fill(ds);
                return ds;
            }
            catch (Exception e) {
                int x = 3;
                return null;
            }
            finally {
                try { cmd.Dispose(); }
                catch { }
                try { connection.Close(); }
                catch { };
            }
        }

        /// <summary>
        /// Performs a query to the database.  Note: this is for a single SELECT command, not for a stored procedure
        /// </summary>
        /// <param name="queryString"></param>
        /// <param name="connectionString"></param>
        /// <returns></returns>
        public static DataSet getDataSetFromQuery(string queryString, string connectionString) {
            SqlConnection connection = null;
            SqlCommand command = null;
            try {
                connection = new SqlConnection(connectionString);
                connection.Open();
                command = new SqlCommand(queryString,connection);
                command.CommandType = CommandType.Text;
                DataSet ds = new DataSet();
                DataAdapter da = new SqlDataAdapter(command);
                da.Fill(ds);
                return ds;
            }  catch (Exception e) {
                //Update1.bubba.Write("Oops.  We got an Exception.  Exception.Message:"+e.Message+" Exception statck track:"+e.StackTrace);
               // Update1.bubba.End();
                int x=3;
                return null;
            }  finally {
                try { command.Dispose(); } catch { }
                try { connection.Close(); } catch { };
            }
        }
        /// <summary>
        /// Takes a give string and returns a new string which is padded to outputSTringSize length, and fills it with padFillCaracter. 
        /// </summary>
        /// <param name="source"></param>
        /// <param name="padDirection">Padding left PAD_DIRECTION.LEFT vs Padding right PAD_DIRECTION_RIGHT</param>
        /// <param name="outputStringSize"></param>
        /// <param name="padFillCharacter"></param>
        /// <returns></returns>

        public static string PadString(string source, PAD_DIRECTION padDirection, int outputStringSize, char padFillCharacter) {
            if (source.Length >= outputStringSize) {
                return source;
            } else {
                StringBuilder sb = new StringBuilder();
                if (padDirection.Equals(PAD_DIRECTION.LEFT)) {
                    for (int c = 0; c < (outputStringSize - source.Length); c++) {
                        sb.Append(padFillCharacter);
                    }
                    sb.Append(source);
                } else {
                    sb.Append(source);
                    for (int c = 0; c < (outputStringSize - source.Length); c++) {
                        sb.Append(padFillCharacter);
                    }
                }
                return sb.ToString();
            }
        }
        public static bool isNothing(object obj) {
            if (obj == null) {
                return true;
            } else {
                if (obj is string) {
                    return ((string)obj).Trim().Equals(string.Empty);
                } else {
                    if (obj is DBNull) {
                        return true;
                    } else {
                        if (obj is Int32) {
                            return ((int)obj) == NULL_INT;
                        } else {
                            if (obj is DateTime) {
                                return ((DateTime)obj).Equals(NULL_DATETIME) || ((DateTime)obj).Equals(SQL_NULL_DATETIME);
                            } else {
                                if (obj is Decimal) {
                                    return ((Decimal)obj) == NULL_DECIMAL;
                                }
                                return false;
                            }
                        }
                    }
                }
            }
        }
        public static DateTime ObjectToDateTime(object obj) {
            if (obj == null) {
                return SQL_NULL_DATETIME;
            } else {
                if (obj is DBNull) {
                    return SQL_NULL_DATETIME;
                } else {
                    try {
                        return Convert.ToDateTime(obj);
                    } catch {
                        return SQL_NULL_DATETIME;
                    }
                }
            }
        }
        public static DateTime? ObjectToDateTimeNullable(object obj) {
            if (obj == null) {
                return null;
            } else {
                if (obj is DBNull) {
                    return null;
                } else {
                    try {
                        return Convert.ToDateTime(obj);
                    } catch {
                        return null;
                    }
                }
            }
        }

        public static string ObjectToString(object obj) {
            if (obj == null) {
                return "";
            } else {
                if (obj is System.Xml.XmlAttribute) {
                    return ((System.Xml.XmlAttribute)obj).Value.ToString();
                }
                if (obj is string) {
                    return ( (obj.ToString().ToLower().Equals("none")?"":(string)obj));
                } else {
                    if (obj is DBNull) {
                        return "";
                    } else {
                        return obj.ToString();
                    }
                }
            }
        }
        public static decimal ObjectTodecimal(object obj) {
            return ObjectToDecimal(obj);
        }

        public static bool hasData(DataSet ds) {
            return
                ds != null &&
                ds.Tables != null &&
                ds.Tables.Count > 0 &&
                ds.Tables[0].Rows != null &&
                ds.Tables[0].Rows.Count > 0;
        }

        public static int ObjectToIntNULLINTIfNull(object obj) {
            if (obj == null || obj is DBNull) {
                return NULL_INT;
            } else {
                return ObjectToInt(obj);
            }
        }
        public static decimal ObjectToDecimal(object obj) {
            try {
                if (obj is DBNull || obj == null) {
                    return NULL_DECIMAL;
                } else {
                    return Convert.ToDecimal(obj);
                }
            } catch {
                return 0;
            }
        }
        public static decimal ObjectToDecimal0IfNull(object obj) {
            try {
                if (obj is DBNull || obj == null) {
                    return 0;
                } else {
                    return Convert.ToDecimal(obj);
                }
            } catch {
                return 0;
            }
        }
        public static double ObjectToDouble(object obj) {
            try {
                return Convert.ToDouble(obj);
            } catch {
                return 0d;
            }
        }
        public static int ObjectToInt(object obj) {
            try {
                return Convert.ToInt32(obj);
            } catch {
                return 0;
            }
        }

        public static bool ObjectToBool(object obj) {

            if (obj is string) {
                string str = ((string)obj).Trim().ToLower();
                if (str.Equals("t")) {
                    return true;
                } else {
                    if ((str.Equals("true"))) {
                        return true;
                    } else {
                        if (str.Equals("y")) {
                            return true;
                        } else {
                            if (str.Equals("yes")) {
                                return true;
                            } else {
                                if (str.Trim().Equals("x")) {
                                    return true;
                                }
                            }
                        }
                    }
                }
                return false;
            }
            try {
                return Convert.ToBoolean(obj);
            } catch {
                return false;
            }
        }

    }
}
