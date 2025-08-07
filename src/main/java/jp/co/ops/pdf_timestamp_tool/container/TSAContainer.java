package jp.co.ops.pdf_timestamp_tool.container;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;

import org.bouncycastle.tsp.TSPException;

import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.security.ExternalSignatureContainer;
import com.itextpdf.text.pdf.security.TSAClientBouncyCastle;

/**
 * ExternalSignatureContainer の実装クラス。
 * PDFファイルに対してRFC3161タイムスタンプトークンを付与するための署名コンテナ。
 */
public class TSAContainer implements ExternalSignatureContainer {
	private TSAClientBouncyCastle tsaClient;
	
	/**
	 * コンストラクタ。
	 * @param tsaClient タイムスタンプトークン取得用の TSAClientBouncyCastle インスタンス
	 */
	public TSAContainer(TSAClientBouncyCastle tsaClient) {
		this.tsaClient = tsaClient;
	}

	/**
	 * 署名辞書にタイムスタンプ署名フィルター設定
	 */
	@Override
	public void modifySigningDictionary(PdfDictionary signDic) {
		signDic.put(PdfName.FILTER, new PdfName("Adobe.PPKLite"));
		signDic.put(PdfName.SUBFILTER, new PdfName("ETSI.RFC3161"));
	}

	/**
	 * PDF署名対象ストリームに対してタイムスタンプトークンを取得
	 */
	@Override
	public byte[] sign(InputStream is) throws GeneralSecurityException {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] buffer = new byte[8192];
			int length;
			while ((length = is.read(buffer)) != -1) {
				digest.update(buffer, 0, length);
			}
			byte[] hash = digest.digest();

			byte[] tsToken;
			try {
				// タイムスタンプトークンを取得
				tsToken = tsaClient.getTimeStampToken(hash);
			} catch (TSPException e) {
				throw new GeneralSecurityException("TSPException occurred", e);
			}
			return tsToken;
		} catch (IOException e) {
			throw new GeneralSecurityException("Error reading input stream", e);
		}
	}
}
