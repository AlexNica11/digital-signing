package com.ds.dsms;

import com.ds.dsms.dss.DSSAPI;
import com.ds.dsms.dss.keystore.KeyStoreParams;
import com.ds.dsms.dss.keystore.PrivateKeyParams;
import eu.europa.esig.dss.diagnostic.DiagnosticData;
import eu.europa.esig.dss.diagnostic.SignatureWrapper;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.validation.SignedDocumentValidator;
import eu.europa.esig.dss.validation.reports.Reports;
import org.apache.commons.io.FileUtils;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.util.List;

class DsmsApplicationTests {

	@Test
	void contextLoads() {
	}

	/*
	good-user-crl-ocsp.p12
	good-user-crl-ocsp
	ks-password

	signer-key-store.p12
	signer-cert-alias
	password

	signedDocument - Copy (2)
	 */
	@Test
	void testPades() throws IOException {
		String resourcesPath = "src/test/resources/";
		byte[] keyStore = FileUtils.readFileToByteArray(new File(resourcesPath + "good-user-crl-ocsp.p12"));
		byte[] unsignedDocument = FileUtils.readFileToByteArray(new File(resourcesPath + "signedDocument - Copy (2).pdf"));
		PrivateKeyParams privateKeyParams = new PrivateKeyParams("good-user-crl-ocsp", null);
		KeyStoreParams keyStoreParams = new KeyStoreParams(keyStore, "ks-password", List.of(privateKeyParams));
		DSSAPI dssapi = new DSSAPI();
		byte[] signedPades = dssapi.signPades(unsignedDocument, List.of(keyStoreParams), SignatureLevel.PAdES_BASELINE_LTA, false, true);
		FileUtils.writeByteArrayToFile(new File(resourcesPath + "signedDocument.pdf"), signedPades);

		testDocument(signedPades);
	}

	@Test
	void testXades() throws Exception {
		String resourcesPath = "src/test/resources/";
		byte[] keyStore = FileUtils.readFileToByteArray(new File(resourcesPath + "good-user-crl-ocsp.p12"));
		byte[] unsignedDocument = FileUtils.readFileToByteArray(new File(resourcesPath + "signedXml - Copy.xml"));
		PrivateKeyParams privateKeyParams = new PrivateKeyParams("good-user-crl-ocsp", null);
		KeyStoreParams keyStoreParams = new KeyStoreParams(keyStore, "ks-password", List.of(privateKeyParams));
		DSSAPI dssapi = new DSSAPI();
		byte[] signXades = dssapi.signXades(unsignedDocument, List.of(keyStoreParams), SignatureLevel.XAdES_BASELINE_LTA, SignaturePackaging.ENVELOPED, true);
		FileUtils.writeByteArrayToFile(new File(resourcesPath + "signedXml.xml"), signXades);

		testDocument(signXades);
	}

	@Test
	void testCades() throws Exception {
		String resourcesPath = "src/test/resources/";
		byte[] keyStore = FileUtils.readFileToByteArray(new File(resourcesPath + "good-user-crl-ocsp.p12"));
		byte[] unsignedDocument = FileUtils.readFileToByteArray(new File(resourcesPath + "xml_example.xml"));
		PrivateKeyParams privateKeyParams = new PrivateKeyParams("good-user-crl-ocsp", null);
		KeyStoreParams keyStoreParams = new KeyStoreParams(keyStore, "ks-password", List.of(privateKeyParams));
		DSSAPI dssapi = new DSSAPI();
		byte[] signCades = dssapi.signCades(unsignedDocument, List.of(keyStoreParams), SignatureLevel.CAdES_BASELINE_LTA, SignaturePackaging.ENVELOPING, false);
		FileUtils.writeByteArrayToFile(new File(resourcesPath + "signedXml.p7m"), signCades);

		testDocument(signCades);
	}

	@Test
	void testJades() throws Exception {
		String resourcesPath = "src/test/resources/";
		byte[] keyStore = FileUtils.readFileToByteArray(new File(resourcesPath + "good-user-crl-ocsp.p12"));
		byte[] unsignedDocument = FileUtils.readFileToByteArray(new File(resourcesPath + "xml_example.xml"));
		PrivateKeyParams privateKeyParams = new PrivateKeyParams("good-user-crl-ocsp", null);
		KeyStoreParams keyStoreParams = new KeyStoreParams(keyStore, "ks-password", List.of(privateKeyParams));
		DSSAPI dssapi = new DSSAPI();
		byte[] signJades = dssapi.signJades(unsignedDocument, List.of(keyStoreParams), SignatureLevel.JAdES_BASELINE_LTA, SignaturePackaging.ENVELOPING, false);
		FileUtils.writeByteArrayToFile(new File(resourcesPath + "signedXml.json"), signJades);

		testDocument(signJades);
	}

	private void testDocument(byte[] document){
		DSSDocument inMemoryDocument = new InMemoryDocument(document);
		SignedDocumentValidator documentValidator = SignedDocumentValidator.fromDocument(inMemoryDocument);
		documentValidator.setCertificateVerifier(new CommonCertificateVerifier());
		Reports reports = documentValidator.validateDocument();
		DiagnosticData diagnosticData = reports.getDiagnosticData();
		List<SignatureWrapper> signatures = diagnosticData.getSignatures();
		for (SignatureWrapper signature : signatures) {
			System.out.println("B: " + signature.isBLevelTechnicallyValid());
			System.out.println("T: " + signature.isTLevelTechnicallyValid());
			System.out.println("A: " + signature.isALevelTechnicallyValid());
			assertTrue(signature.isBLevelTechnicallyValid());
		}
	}

}
