package jp.co.ops.pdf_timestamp_tool.service;

import java.io.FileOutputStream;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.security.CrlClientOnline;
import com.itextpdf.text.pdf.security.LtvVerification;
import com.itextpdf.text.pdf.security.OcspClientBouncyCastle;

public class LtvEnableService {

	public void enableLtv(String signedPdfPath, String ltvEnabledPdfPath) throws Exception {

		// 署名済みPDFを読み込み
		PdfReader reader = new PdfReader(signedPdfPath);
		FileOutputStream os = new FileOutputStream(ltvEnabledPdfPath);

		// LTV対応PDFを作成
		PdfStamper stamper = PdfStamper.createSignature(reader, os, '\0', null, true);
		LtvVerification ltv = stamper.getLtvVerification();

		// OCSP / CRL クライアントを生成
		OcspClientBouncyCastle ocsp = new OcspClientBouncyCastle();
		CrlClientOnline crl = new CrlClientOnline();

		// LTV有効化実行
		boolean result = ltv.addVerification(
			"InvisibleSignature", // PdfTimestampServiceで設定した署名フィールド名
			ocsp,
			crl,
			LtvVerification.CertificateOption.SIGNING_CERTIFICATE,
			LtvVerification.Level.OCSP_CRL,
			LtvVerification.CertificateInclusion.YES
		);

		System.out.println("LTV追加結果: " + result);

		stamper.close();
		reader.close();
	}
}
