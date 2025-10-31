package jp.co.ops.pdf_timestamp_tool.container;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.bouncycastle.tsp.TSPException;

import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.security.DigestAlgorithms;
import com.itextpdf.text.pdf.security.ExternalSignatureContainer;
import com.itextpdf.text.pdf.security.TSAClientBouncyCastle;

/**
 * タイムスタンプトークンを生成し、TSA証明書を取得するクラス。
 * iText 5.5.13.3 + BouncyCastle 1.70 対応。
 */
public class TSAContainer implements ExternalSignatureContainer {

	private final TSAClientBouncyCastle tsaClient;
	private final String tsaUrl;
	private X509Certificate tsaCertificate;

	public TSAContainer(TSAClientBouncyCastle tsaClient) {
		this.tsaClient = tsaClient;
		this.tsaUrl = extractTsaUrl(tsaClient);
	}

	// 🔹 URLをリフレクションで取得
	private String extractTsaUrl(TSAClientBouncyCastle client) {
		try {
			java.lang.reflect.Field field = client.getClass().getDeclaredField("tsaURL");
			field.setAccessible(true);
			Object value = field.get(client);
			return value != null ? value.toString() : "";
		} catch (Exception e) {
			System.err.println("[WARN] TSA URL取得失敗: " + e.getMessage());
			return "";
		}
	}

	public String getTsaUrl() {
		return tsaUrl;
	}

	public X509Certificate getCertificate() {
		return tsaCertificate;
	}

	@Override
	public byte[] sign(InputStream data) throws GeneralSecurityException {
		byte[] tsToken = new byte[0];

		try {
			// 🔹 データのハッシュ化（SHA-256）
			byte[] digest = DigestAlgorithms.digest(data, "SHA-256", null);

			// 🔹 タイムスタンプトークン生成
			tsToken = tsaClient.getTimeStampToken(digest);

			// 🔹 TSA証明書取得（SSL.com対応）
			if (tsaUrl != null && tsaUrl.startsWith("http")) {
				try {
					URL url = new URL(tsaUrl);
					try (InputStream is = url.openStream()) {
						CertificateFactory cf = CertificateFactory.getInstance("X.509");
						tsaCertificate = (X509Certificate) cf.generateCertificate(is);
						System.out.println("[INFO] TSA証明書を取得しました: " + tsaCertificate.getSubjectDN());
					}
				} catch (IOException e) {
					System.err.println("[WARN] TSA証明書取得エラー: " + e.getMessage());
				}
			} else {
				System.out.println("[WARN] TSA URLが無効のため証明書を取得できません。");
			}
		} catch (IOException e) {
			System.err.println("[ERROR] タイムスタンプ通信エラー(IO): " + e.getMessage());
		} catch (TSPException e) {
			System.err.println("[ERROR] タイムスタンプトークン生成エラー(TSP): " + e.getMessage());
		} catch (Exception e) {
			System.err.println("[ERROR] 想定外のエラー: " + e.getMessage());
		}

		return tsToken;
	}

	@Override
	public void modifySigningDictionary(PdfDictionary signDic) {
		signDic.put(PdfName.FILTER, PdfName.ADOBE_PPKLITE);
		signDic.put(PdfName.SUBFILTER, PdfName.ETSI_RFC3161);
	}
}
