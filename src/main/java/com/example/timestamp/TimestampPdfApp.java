package com.example.timestamp;

import java.io.File;
import java.io.FileOutputStream;
import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.signatures.ITSAClient;
import com.itextpdf.signatures.PdfSigner;
import com.itextpdf.signatures.TSAClientBouncyCastle;

public class TimestampPdfApp {

	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			System.out.println("使い方: java -jar pdf-timestamp-tool.jar <PDFファイル>");
			return;
		}

		String inputPath = args[0];
		String outputPath = inputPath.replace(".pdf", "_timestamped.pdf");

		File inputFile = new File(inputPath);
		if (!inputFile.exists()) {
			System.out.println("ファイルが存在しません: " + inputPath);
			return;
		}

		// セキュリティプロバイダの登録（BouncyCastle）
		Security.addProvider(new BouncyCastleProvider());

		// タイムスタンプ用 TSA クライアント
		ITSAClient tsa = new TSAClientBouncyCastle("http://timestamp.sectigo.com");

		// 読み込みと出力の設定
		PdfReader reader = new PdfReader(inputFile);
		PdfSigner signer = new PdfSigner(reader,
			new FileOutputStream(outputPath),
			new StampingProperties().useAppendMode());

		// フィールド名を指定（なくても動くが一応設定）
		signer.setFieldName("TSA_Signature");

		// フィールド名を指定
		String fieldName = "TSA_Signature";
		signer.setFieldName(fieldName);

		// 正しい形式でタイムスタンプを付与
		signer.timestamp(tsa, fieldName);

		System.out.println("タイムスタンプを付与しました: " + outputPath);
	}
}
