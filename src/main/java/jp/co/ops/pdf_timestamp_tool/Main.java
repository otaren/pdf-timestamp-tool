package jp.co.ops.pdf_timestamp_tool;

import java.io.File;

import com.itextpdf.text.pdf.security.TSAClientBouncyCastle;

import jp.co.ops.pdf_timestamp_tool.container.TSAContainer;
import jp.co.ops.pdf_timestamp_tool.service.PdfTimestampService;

public class Main {

	public static void main(String[] args) {
		File inputDir = new File("input");
		File outputDir = new File("output");

		if (!outputDir.exists()) {
			outputDir.mkdirs();
		}

		File[] pdfFiles = inputDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));
		if (pdfFiles == null || pdfFiles.length == 0) {
			System.out.println("inputフォルダにPDFがありません。");
			return;
		}

		// 🔹 SSL.com の TSA サーバーURL
		String tsaUrl = "http://ts.ssl.com";

		for (File pdfFile : pdfFiles) {
			String outputPath = "output/" + pdfFile.getName();
			System.out.println("タイムスタンプ付与中: " + pdfFile.getName());

			try {
				// 🔹 TSAクライアント生成（SSL.com対応）
				TSAClientBouncyCastle tsaClient = new TSAClientBouncyCastle(tsaUrl);
				TSAContainer tsaContainer = new TSAContainer(tsaClient);

				// 🔹 PDFタイムスタンプ付与
				PdfTimestampService service = new PdfTimestampService();
				service.addTimestamp(pdfFile.getAbsolutePath(), outputPath, tsaContainer);

				System.out.println("完了: " + pdfFile.getName());
			} catch (Exception e) {
				System.err.println("失敗: " + pdfFile.getName());
				e.printStackTrace();
			}
		}

		System.out.println("全PDFへのタイムスタンプ付与が完了しました。");
	}
}
