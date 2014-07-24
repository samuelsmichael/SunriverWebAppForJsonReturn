using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Data.SqlClient;
using System.Data.Common;
using System.Data;
using System.Runtime.Serialization;

namespace SunriverWebApp {
    [DataContractAttribute]
    public class GISLayers : WebServiceItem {
        [DataMemberAttribute]
        public int srGISLayersId { get; set; }
        [DataMemberAttribute]
        public int srGISLayersUseNum { get; set; }
        [DataMemberAttribute]
        public string srGISLayersURL { get; set; }
        [DataMemberAttribute]
        public bool srGISLayersIsBikePaths { get; set; }

        protected override WebServiceItem objectFromDatasetRow(System.Data.DataRow dr) {
            GISLayers gisLayers = new GISLayers();
            gisLayers.srGISLayersId=Utils.ObjectToInt(dr["srGISLayersId"]);
            gisLayers.srGISLayersIsBikePaths = Utils.ObjectToBool(dr["srGISLayersIsBikePaths"]);
            gisLayers.srGISLayersURL = Utils.ObjectToString(dr["srGISLayersURL"]);
            gisLayers.srGISLayersUseNum = Utils.ObjectToInt(dr["srGISLayersUseNum"]);
            return gisLayers;
        }

        public static List<GISLayers> Sample {
            get {
                List<GISLayers> list = new List<GISLayers>();
                list.Add(new GISLayers {
                    srGISLayersId = 100,
                    srGISLayersUseNum = 1,
                    srGISLayersIsBikePaths=true,
                    srGISLayersURL = "http://tiles.arcgis.com/tiles/PPpMbTaRDuKoF0e4/arcgis/rest/services/SRPathways/MapServer"
                });
                list.Add(new GISLayers {
                    srGISLayersId = 200,
                    srGISLayersUseNum=0,
                    srGISLayersIsBikePaths=false,
                    srGISLayersURL = "http://services.arcgisonline.com/ArcGIS/rest/services/USA_Topo_Maps/MapServer"
                });
                return list;
            }
        }

        public List<GISLayers> buildList() {
            List<GISLayers> list = new List<GISLayers>();
            foreach (DataRow dr in getDataSet().Tables[0].Rows) {
                list.Add((GISLayers)objectFromDatasetRow(dr));
            }
            return list;
        }
    }
}
