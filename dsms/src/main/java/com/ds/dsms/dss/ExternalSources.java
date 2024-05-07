package com.ds.dsms.dss;

import eu.europa.esig.dss.service.http.commons.TimestampDataLoader;
import eu.europa.esig.dss.service.tsp.OnlineTSPSource;
import eu.europa.esig.dss.spi.x509.CertificateSource;
import eu.europa.esig.dss.spi.x509.CommonTrustedCertificateSource;
import eu.europa.esig.dss.spi.x509.tsp.CompositeTSPSource;
import eu.europa.esig.dss.spi.x509.tsp.TSPSource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExternalSources {
    protected static Map<String, String> tspSources = new ConcurrentHashMap<>();
    private static CompositeTSPSource tspSource;
    private static CommonTrustedCertificateSource trustedCertificateSource;

    static {
        tspSources.put("TSP1", "https://freetsa.org/tsr");
        tspSources.put("TSP2", "https://freetsa.org/tsr");
        tspSources.put("TSP3", "https://freetsa.org/tsr");
    }

    protected static TSPSource getOnlineTSPSource(){
        if(tspSource == null) {
            tspSource = new CompositeTSPSource();
            TimestampDataLoader dataLoader = new TimestampDataLoader();
            dataLoader.setTimeoutConnection(10000);
            dataLoader.setTimeoutSocket(10000);
            Map<String, TSPSource> tspSourcesMap = new ConcurrentHashMap<>();
            for (Map.Entry<String, String> tspSourceEntry : ExternalSources.tspSources.entrySet()) {
                OnlineTSPSource onlineTSPSource = new OnlineTSPSource(tspSourceEntry.getValue());
                onlineTSPSource.setDataLoader(dataLoader);
                tspSourcesMap.put(tspSourceEntry.getKey(), onlineTSPSource);
            }
            tspSource.setTspSources(tspSourcesMap);
        }
        return tspSource;
    }

    protected static CertificateSource getTrustedCertificateSource() {
        if (trustedCertificateSource == null) {
            trustedCertificateSource = new CommonTrustedCertificateSource();
        }
        return trustedCertificateSource;
    }
}
