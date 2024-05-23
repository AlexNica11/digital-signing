package com.ds.dsms.dss;

import com.ds.dsms.dss.keystore.KeyStoreParams;
import com.ds.dsms.dss.keystore.PrivateKeyParams;
import eu.europa.esig.dss.cades.CAdESSignatureParameters;
import eu.europa.esig.dss.cades.signature.CAdESService;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.JWSSerializationType;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.jades.JAdESSignatureParameters;
import eu.europa.esig.dss.jades.signature.JAdESService;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.model.SignatureValue;
import eu.europa.esig.dss.model.ToBeSigned;
import eu.europa.esig.dss.pades.PAdESSignatureParameters;
import eu.europa.esig.dss.pades.signature.PAdESService;
import eu.europa.esig.dss.service.crl.OnlineCRLSource;
import eu.europa.esig.dss.service.http.commons.CommonsDataLoader;
import eu.europa.esig.dss.service.http.commons.FileCacheDataLoader;
import eu.europa.esig.dss.service.http.commons.OCSPDataLoader;
import eu.europa.esig.dss.service.ocsp.OnlineOCSPSource;
import eu.europa.esig.dss.spi.DSSUtils;
import eu.europa.esig.dss.spi.tsl.TrustedListsCertificateSource;
import eu.europa.esig.dss.spi.x509.KeyStoreCertificateSource;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.Pkcs12SignatureToken;
import eu.europa.esig.dss.token.SignatureTokenConnection;
import eu.europa.esig.dss.tsl.cache.CacheCleaner;
import eu.europa.esig.dss.tsl.job.TLValidationJob;
import eu.europa.esig.dss.tsl.source.LOTLSource;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.xades.XAdESSignatureParameters;
import eu.europa.esig.dss.xades.signature.XAdESService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.util.Pair;

import java.io.File;
import java.security.KeyStore.PasswordProtection;
import java.util.List;
import java.util.Set;

public class DSSAPI {

    public Pair<byte[], String> signDocument(byte[] unsignedDocument, Set<KeyStoreParams> keyStores, String signatureLevel, boolean extendSignature){
        if(signatureLevel.startsWith("PAdES")){
            return Pair.of(signPades(unsignedDocument, keyStores, SignatureLevel.valueByName(signatureLevel), extendSignature),
                    ".pdf");
        } else if(signatureLevel.startsWith("XAdES")){
            return Pair.of(signXades(unsignedDocument, keyStores, SignatureLevel.valueByName(signatureLevel), SignaturePackaging.ENVELOPING, extendSignature),
                    ".xml");
        } else if(signatureLevel.startsWith("JAdES")){
            return Pair.of(signJades(unsignedDocument, keyStores, SignatureLevel.valueByName(signatureLevel), SignaturePackaging.ENVELOPING, extendSignature),
                    ".json");
        } else {
            return Pair.of(signCades(unsignedDocument, keyStores, SignatureLevel.valueByName(signatureLevel), SignaturePackaging.ENVELOPING, extendSignature),
                    ".p7m");
        }
    }


    // add support for visible signatures, B T LT LTA level support, other signatures
    // maybe add support to extend signatures
    private byte[] signPades(byte[] unsignedDocument, Set<KeyStoreParams> keyStores, SignatureLevel signatureLevel, boolean extendSignature){
        DSSDocument signedDocument = new InMemoryDocument(unsignedDocument);
        CommonCertificateVerifier commonCertificateVerifier = new CommonCertificateVerifier();
        PAdESService padesService = new PAdESService(commonCertificateVerifier);

        for(KeyStoreParams keyStore : keyStores){
            try (Pkcs12SignatureToken signingToken = new Pkcs12SignatureToken(keyStore.getKeyStoreBytes(), new PasswordProtection(keyStore.getKeyStorePassword().toCharArray()))) {

                // Preparing parameters for the PAdES signature
                PAdESSignatureParameters parameters = initSignatureParameters();
//                parameters.setSignatureLevel(signatureLevel);
                Set<PrivateKeyParams> privateKeyParams = keyStore.getPrivateKeyParams();
                for(PrivateKeyParams privateKeyParam : privateKeyParams) {
                    // Set the signing certificate and a certificate chain for the used token
                    DSSPrivateKeyEntry privateKey = null;

                    if(StringUtils.isBlank(privateKeyParam.getPassword())){
                        privateKey = signingToken.getKey(privateKeyParam.getAlias());
                    } else {
                        privateKey = signingToken.getKey(privateKeyParam.getAlias(), new PasswordProtection(privateKeyParam.getPassword().toCharArray()));
                    }

                    parameters.setSigningCertificate(privateKey.getCertificate());
                    parameters.setCertificateChain(privateKey.getCertificateChain());

                    // Sign in three steps
                    ToBeSigned dataToSign = padesService.getDataToSign(signedDocument, parameters);

                    SignatureValue signatureValue = signingToken.sign(dataToSign, parameters.getDigestAlgorithm(), privateKey);
                    if(!extendSignature) {
                        signedDocument = padesService.signDocument(signedDocument, parameters, signatureValue);
                    }

                    if(SignatureLevel.PAdES_BASELINE_T.equals(signatureLevel)){
                        parameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_T);
                        // init TSP source for timestamp requesting
                        padesService.setTspSource(ExternalSources.getOnlineTSPSource());
                        signedDocument = padesService.extendDocument(signedDocument, parameters);
                    }

                    if(SignatureLevel.PAdES_BASELINE_LT.equals(signatureLevel) || SignatureLevel.PAdES_BASELINE_LTA.equals(signatureLevel)){
                        parameters.setSignatureLevel(signatureLevel);
                        // init revocation sources for CRL/OCSP requesting
                        commonCertificateVerifier.setCrlSource(new OnlineCRLSource());
                        commonCertificateVerifier.setOcspSource(new OnlineOCSPSource());
                        // Trust anchors should be defined for revocation data requesting
                        commonCertificateVerifier.setTrustedCertSources(ExternalSources.getTrustedCertificateSource());

                        // Might be a security issue
                        // For test purpose (not recommended for use in production)
                        //TODO
                        commonCertificateVerifier.setCheckRevocationForUntrustedChains(true);

                        // init TSP source for timestamp requesting
                        padesService.setTspSource(ExternalSources.getOnlineTSPSource());
                        signedDocument = padesService.extendDocument(signedDocument, parameters);
                    }
                }
            }
        }

        return DSSUtils.toByteArray(signedDocument);
    }

    private byte[] signXades(byte[] unsignedDocument, Set<KeyStoreParams> keyStores, SignatureLevel signatureLevel, SignaturePackaging signaturePackaging, boolean extendSignature) {
        DSSDocument signedDocument = new InMemoryDocument(unsignedDocument);
        CommonCertificateVerifier commonCertificateVerifier = new CommonCertificateVerifier();
        XAdESService xadesService = new XAdESService(commonCertificateVerifier);

        for(KeyStoreParams keyStore : keyStores) {
            try (Pkcs12SignatureToken signingToken = new Pkcs12SignatureToken(keyStore.getKeyStoreBytes(), new PasswordProtection(keyStore.getKeyStorePassword().toCharArray()))) {
                // Preparing parameters for the XAdES signature
                XAdESSignatureParameters parameters = new XAdESSignatureParameters();
                Set<PrivateKeyParams> privateKeyParams = keyStore.getPrivateKeyParams();

                for(PrivateKeyParams privateKeyParam : privateKeyParams) {
                    // Set the signing certificate and a certificate chain for the used token
                    DSSPrivateKeyEntry privateKey = null;

                    if (StringUtils.isBlank(privateKeyParam.getPassword())) {
                        privateKey = signingToken.getKey(privateKeyParam.getAlias());
                    } else {
                        privateKey = signingToken.getKey(privateKeyParam.getAlias(), new PasswordProtection(privateKeyParam.getPassword().toCharArray()));
                    }

                    // We choose the level of the signature (-B, -T, -LT, -LTA).
                    parameters.setSignatureLevel(SignatureLevel.XAdES_BASELINE_B);
                    // We choose the type of the signature packaging (ENVELOPED, ENVELOPING, DETACHED).
                    parameters.setSignaturePackaging(signaturePackaging);
                    // We set the digest algorithm to use with the signature algorithm. You must use the
                    // same parameter when you invoke the method sign on the token. The default value is SHA256
                    parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);

                    // We set the signing certificate
                    parameters.setSigningCertificate(privateKey.getCertificate());
                    // We set the certificate chain
                    parameters.setCertificateChain(privateKey.getCertificateChain());

                    // Sign in three steps
                    ToBeSigned dataToSign = xadesService.getDataToSign(signedDocument, parameters);
                    SignatureValue signatureValue = signingToken.sign(dataToSign, parameters.getDigestAlgorithm(), privateKey);

                    if(!extendSignature) {
                        signedDocument = xadesService.signDocument(signedDocument, parameters, signatureValue);
                    }

                    if(SignatureLevel.XAdES_BASELINE_T.equals(signatureLevel)){
                        parameters.setSignatureLevel(SignatureLevel.XAdES_BASELINE_T);
                        // init TSP source for timestamp requesting
                        xadesService.setTspSource(ExternalSources.getOnlineTSPSource());
                        signedDocument = xadesService.extendDocument(signedDocument, parameters);
                    }

                    if(SignatureLevel.XAdES_BASELINE_LT.equals(signatureLevel) || SignatureLevel.XAdES_BASELINE_LTA.equals(signatureLevel)){
                        parameters.setSignatureLevel(signatureLevel);
                        // init revocation sources for CRL/OCSP requesting
                        commonCertificateVerifier.setCrlSource(new OnlineCRLSource());
                        commonCertificateVerifier.setOcspSource(new OnlineOCSPSource());
                        // Trust anchors should be defined for revocation data requesting
                        commonCertificateVerifier.setTrustedCertSources(ExternalSources.getTrustedCertificateSource());

                        // Might be a security issue
                        // For test purpose (not recommended for use in production)
                        //TODO
                        commonCertificateVerifier.setCheckRevocationForUntrustedChains(true);

                        // init TSP source for timestamp requesting
                        xadesService.setTspSource(ExternalSources.getOnlineTSPSource());
                        signedDocument = xadesService.extendDocument(signedDocument, parameters);
                    }
                }
            }
        }

        return DSSUtils.toByteArray(signedDocument);
    }

    private byte[] signCades(byte[] unsignedDocument, Set<KeyStoreParams> keyStores, SignatureLevel signatureLevel, SignaturePackaging signaturePackaging, boolean extendSignature) {
        DSSDocument signedDocument = new InMemoryDocument(unsignedDocument);
        CommonCertificateVerifier commonCertificateVerifier = new CommonCertificateVerifier();
        CAdESService cadesService = new CAdESService(commonCertificateVerifier);

        for(KeyStoreParams keyStore : keyStores) {
            try (Pkcs12SignatureToken signingToken = new Pkcs12SignatureToken(keyStore.getKeyStoreBytes(), new PasswordProtection(keyStore.getKeyStorePassword().toCharArray()))) {
                // Preparing parameters for the CAdES signature
                CAdESSignatureParameters parameters = new CAdESSignatureParameters();
                Set<PrivateKeyParams> privateKeyParams = keyStore.getPrivateKeyParams();

                for(PrivateKeyParams privateKeyParam : privateKeyParams) {
                    // Set the signing certificate and a certificate chain for the used token
                    DSSPrivateKeyEntry privateKey = null;

                    if (StringUtils.isBlank(privateKeyParam.getPassword())) {
                        privateKey = signingToken.getKey(privateKeyParam.getAlias());
                    } else {
                        privateKey = signingToken.getKey(privateKeyParam.getAlias(), new PasswordProtection(privateKeyParam.getPassword().toCharArray()));
                    }

                    // We choose the level of the signature (-B, -T, -LT, -LTA).
                    parameters.setSignatureLevel(SignatureLevel.CAdES_BASELINE_B);
                    // We choose the type of the signature packaging (ENVELOPED, ENVELOPING, DETACHED).
                    parameters.setSignaturePackaging(signaturePackaging);
                    // We set the digest algorithm to use with the signature algorithm. You must use the
                    // same parameter when you invoke the method sign on the token. The default value is SHA256
                    parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);

                    // We set the signing certificate
                    parameters.setSigningCertificate(privateKey.getCertificate());
                    // We set the certificate chain
                    parameters.setCertificateChain(privateKey.getCertificateChain());

                    // Sign in three steps
                    ToBeSigned dataToSign = cadesService.getDataToSign(signedDocument, parameters);
                    SignatureValue signatureValue = signingToken.sign(dataToSign, parameters.getDigestAlgorithm(), privateKey);

                    if(!extendSignature) {
                        signedDocument = cadesService.signDocument(signedDocument, parameters, signatureValue);
                    }

                    if(SignatureLevel.CAdES_BASELINE_T.equals(signatureLevel)){
                        parameters.setSignatureLevel(SignatureLevel.CAdES_BASELINE_T);
                        // init TSP source for timestamp requesting
                        cadesService.setTspSource(ExternalSources.getOnlineTSPSource());
                        signedDocument = cadesService.extendDocument(signedDocument, parameters);
                    }

                    if(SignatureLevel.CAdES_BASELINE_LT.equals(signatureLevel) || SignatureLevel.CAdES_BASELINE_LTA.equals(signatureLevel)){
                        parameters.setSignatureLevel(signatureLevel);
                        // init revocation sources for CRL/OCSP requesting
                        commonCertificateVerifier.setCrlSource(new OnlineCRLSource());
                        commonCertificateVerifier.setOcspSource(new OnlineOCSPSource());
                        // Trust anchors should be defined for revocation data requesting
                        commonCertificateVerifier.setTrustedCertSources(ExternalSources.getTrustedCertificateSource());

                        // Might be a security issue
                        // For test purpose (not recommended for use in production)
                        //TODO
                        commonCertificateVerifier.setCheckRevocationForUntrustedChains(true);

                        // init TSP source for timestamp requesting
                        cadesService.setTspSource(ExternalSources.getOnlineTSPSource());
                        signedDocument = cadesService.extendDocument(signedDocument, parameters);
                    }
                }
            }
        }

//        System.out.println("MimeType: " + signedDocument.getMimeType() + " extension: " + signedDocument.getMimeType().getExtension());

        return DSSUtils.toByteArray(signedDocument);
    }

    private byte[] signJades(byte[] unsignedDocument, Set<KeyStoreParams> keyStores, SignatureLevel signatureLevel, SignaturePackaging signaturePackaging, boolean extendSignature) {
        DSSDocument signedDocument = new InMemoryDocument(unsignedDocument);
        CommonCertificateVerifier commonCertificateVerifier = new CommonCertificateVerifier();
        JAdESService jadesService = new JAdESService(commonCertificateVerifier);

        for(KeyStoreParams keyStore : keyStores) {
            try (Pkcs12SignatureToken signingToken = new Pkcs12SignatureToken(keyStore.getKeyStoreBytes(), new PasswordProtection(keyStore.getKeyStorePassword().toCharArray()))) {
                // Preparing parameters for the JAdES signature
                JAdESSignatureParameters parameters = new JAdESSignatureParameters();
                Set<PrivateKeyParams> privateKeyParams = keyStore.getPrivateKeyParams();

                for(PrivateKeyParams privateKeyParam : privateKeyParams) {
                    // Set the signing certificate and a certificate chain for the used token
                    DSSPrivateKeyEntry privateKey = null;

                    if (StringUtils.isBlank(privateKeyParam.getPassword())) {
                        privateKey = signingToken.getKey(privateKeyParam.getAlias());
                    } else {
                        privateKey = signingToken.getKey(privateKeyParam.getAlias(), new PasswordProtection(privateKeyParam.getPassword().toCharArray()));
                    }

                    // We choose the level of the signature (-B, -T, -LT, -LTA).
                    parameters.setSignatureLevel(SignatureLevel.JAdES_BASELINE_B);
                    // We choose the type of the signature packaging (ENVELOPED, ENVELOPING, DETACHED).
                    parameters.setSignaturePackaging(signaturePackaging);
                    // Choose the form of the signature (COMPACT_SERIALIZATION, JSON_SERIALIZATION, FLATTENED_JSON_SERIALIZATION)
                    parameters.setJwsSerializationType(JWSSerializationType.JSON_SERIALIZATION);
                    // We set the digest algorithm to use with the signature algorithm. You must use the
                    // same parameter when you invoke the method sign on the token. The default value is SHA256
                    parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);

                    // We set the signing certificate
                    parameters.setSigningCertificate(privateKey.getCertificate());
                    // We set the certificate chain
                    parameters.setCertificateChain(privateKey.getCertificateChain());

                    // Sign in three steps
                    ToBeSigned dataToSign = jadesService.getDataToSign(signedDocument, parameters);
                    SignatureValue signatureValue = signingToken.sign(dataToSign, parameters.getDigestAlgorithm(), privateKey);

                    if(!extendSignature) {
                        signedDocument = jadesService.signDocument(signedDocument, parameters, signatureValue);
                    }

                    if(SignatureLevel.JAdES_BASELINE_T.equals(signatureLevel)){
                        parameters.setSignatureLevel(SignatureLevel.JAdES_BASELINE_T);
                        // init TSP source for timestamp requesting
                        jadesService.setTspSource(ExternalSources.getOnlineTSPSource());
                        signedDocument = jadesService.extendDocument(signedDocument, parameters);
                    }

                    if(SignatureLevel.JAdES_BASELINE_LT.equals(signatureLevel) || SignatureLevel.JAdES_BASELINE_LTA.equals(signatureLevel)){
                        parameters.setSignatureLevel(signatureLevel);
                        // init revocation sources for CRL/OCSP requesting
                        commonCertificateVerifier.setCrlSource(new OnlineCRLSource());
                        commonCertificateVerifier.setOcspSource(new OnlineOCSPSource());
                        // Trust anchors should be defined for revocation data requesting
                        commonCertificateVerifier.setTrustedCertSources(ExternalSources.getTrustedCertificateSource());

                        // Might be a security issue
                        // For test purpose (not recommended for use in production)
                        //TODO
                        commonCertificateVerifier.setCheckRevocationForUntrustedChains(true);

                        // init TSP source for timestamp requesting
                        jadesService.setTspSource(ExternalSources.getOnlineTSPSource());
                        signedDocument = jadesService.extendDocument(signedDocument, parameters);
                    }
                }
            }
        }

        System.out.println("MimeType: " + signedDocument.getMimeType() + " extension: " + signedDocument.getMimeType().getExtension());

        return DSSUtils.toByteArray(signedDocument);
    }

    private PAdESSignatureParameters initSignatureParameters() {
        PAdESSignatureParameters parameters = new PAdESSignatureParameters();
        parameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_B);
        parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);
//        parameters.setPermission(CertificationPermission.NO_CHANGE_PERMITTED);
        return parameters;
    }
}
