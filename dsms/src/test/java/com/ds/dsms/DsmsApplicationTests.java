package com.ds.dsms;

import com.ds.dsms.controller.dto.DocumentPayloadDTO;
import com.ds.dsms.dss.DSSAPI;
import com.ds.dsms.dss.keystore.KeyStoreParams;
import com.ds.dsms.dss.keystore.PrivateKeyParams;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.util.Pair;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

class DsmsApplicationTests {
	private String resourcesPath = "src/test/resources/";

	@Test
	void test() throws IOException {
		byte[] unsignedDocument = FileUtils.readFileToByteArray(new File(resourcesPath + "xml_example.xml"));
		String documentName = "xml_example.xml";
		String signature = SignatureLevel.XAdES_BASELINE_LTA.name();
		boolean extendSignature = false;
		byte[] keyStore = FileUtils.readFileToByteArray(new File(resourcesPath + "good-user-crl-ocsp.p12"));
		PrivateKeyParams privateKeyParams = new PrivateKeyParams("good-user-crl-ocsp", null);
		KeyStoreParams keyStoreParams = new KeyStoreParams("good-user-crl-ocsp.p12", keyStore, "ks-password", Set.of(privateKeyParams));
		DocumentPayloadDTO documentPayload = new DocumentPayloadDTO(unsignedDocument, documentName, signature, extendSignature, keyStoreParams, false);
		ObjectMapper objectMapper = new ObjectMapper();
		System.out.println(objectMapper.writeValueAsString(documentPayload));
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
		byte[] keyStore = FileUtils.readFileToByteArray(new File(resourcesPath + "good-user-crl-ocsp.p12"));
		byte[] unsignedDocument = FileUtils.readFileToByteArray(new File(resourcesPath + "signedDocument - Copy (2).pdf"));
		PrivateKeyParams privateKeyParams = new PrivateKeyParams("good-user-crl-ocsp", null);
		KeyStoreParams keyStoreParams = new KeyStoreParams("good-user-crl-ocsp.p12", keyStore, "ks-password", Set.of(privateKeyParams));
		DSSAPI dssapi = new DSSAPI();
		Pair<byte[], String> signedPades = dssapi.signDocument(unsignedDocument, Set.of(keyStoreParams), SignatureLevel.PAdES_BASELINE_LTA.name(), true);
		FileUtils.writeByteArrayToFile(new File(resourcesPath + "signedDocument.pdf"), signedPades.getFirst());

		testDocument(signedPades.getFirst());
	}

	@Test
	void testXades() throws Exception {
		byte[] keyStore = FileUtils.readFileToByteArray(new File(resourcesPath + "good-user-crl-ocsp.p12"));
		byte[] unsignedDocument = FileUtils.readFileToByteArray(new File(resourcesPath + "signedXml - Copy.xml"));
		PrivateKeyParams privateKeyParams = new PrivateKeyParams("good-user-crl-ocsp", null);
		KeyStoreParams keyStoreParams = new KeyStoreParams("good-user-crl-ocsp.p12", keyStore, "ks-password", Set.of(privateKeyParams));
		DSSAPI dssapi = new DSSAPI();
		Pair<byte[], String> signXades = dssapi.signDocument(unsignedDocument, Set.of(keyStoreParams), SignatureLevel.XAdES_BASELINE_LTA.name(), true);
		FileUtils.writeByteArrayToFile(new File(resourcesPath + "signedXml.xml"), signXades.getFirst());

		testDocument(signXades.getFirst());
	}

	@Test
	void testCades() throws Exception {
		byte[] keyStore = FileUtils.readFileToByteArray(new File(resourcesPath + "good-user-crl-ocsp.p12"));
		byte[] unsignedDocument = FileUtils.readFileToByteArray(new File(resourcesPath + "xml_example.xml"));
		PrivateKeyParams privateKeyParams = new PrivateKeyParams("good-user-crl-ocsp", null);
		KeyStoreParams keyStoreParams = new KeyStoreParams("good-user-crl-ocsp.p12", keyStore, "ks-password", Set.of(privateKeyParams));
		DSSAPI dssapi = new DSSAPI();
		Pair<byte[], String> signCades = dssapi.signDocument(unsignedDocument, Set.of(keyStoreParams), SignatureLevel.CAdES_BASELINE_LTA.name(), false);
		FileUtils.writeByteArrayToFile(new File(resourcesPath + "signedXml.p7m"), signCades.getFirst());

		testDocument(signCades.getFirst());
	}

	@Test
	void testJades() throws Exception {
		byte[] keyStore = FileUtils.readFileToByteArray(new File(resourcesPath + "good-user-crl-ocsp.p12"));
		byte[] unsignedDocument = FileUtils.readFileToByteArray(new File(resourcesPath + "xml_example.xml"));
		PrivateKeyParams privateKeyParams = new PrivateKeyParams("good-user-crl-ocsp", null);
		KeyStoreParams keyStoreParams = new KeyStoreParams("good-user-crl-ocsp.p12", keyStore, "ks-password", Set.of(privateKeyParams));
		DSSAPI dssapi = new DSSAPI();
		Pair<byte[], String> signJades = dssapi.signDocument(unsignedDocument, Set.of(keyStoreParams), SignatureLevel.JAdES_BASELINE_LTA.name(), false);
		FileUtils.writeByteArrayToFile(new File(resourcesPath + "signedXml.json"), signJades.getFirst());

		testDocument(signJades.getFirst());
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
