package jp.co.ops.pdf_timestamp_tool;
import java.io.File;

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

		
		String tsaUrl = "https://freetsa.org/tsr";

		for (File pdfFile : pdfFiles) {
			String outputPath = "output/" + pdfFile.getName();

			System.out.println("タイムスタンプ付与中: " + pdfFile.getName());

			try {
				PdfTimestampService.addTimestamp(pdfFile.getAbsolutePath(), outputPath, tsaUrl);
				System.out.println("OK：" + outputPath);
			} catch (Exception e) {
				System.err.println("NG：" + pdfFile.getName() + " → " + e.getMessage());
			}
		}

		System.out.println("全PDFのタイムスタンプ付与が完了しました。");
	}
}
