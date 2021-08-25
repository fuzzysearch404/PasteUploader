package uploader;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.imageio.ImageIO;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import main.Main;


/**
 * This class manages the file upload to the remote services.
 * 
 * @author Roberts Ziedins
 *
 */
public class FileUploader {

	private byte[] bytes;
	private String fileName;
	
	public FileUploader(BufferedImage bufferedImage) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(bufferedImage, Main.DEFAULT_IMAGE_UPLOAD_EXTENSION, baos);
		this.bytes = baos.toByteArray();

		if (Main.customUploadFileName != null && !Main.customUploadFileName.isBlank())
			this.fileName = Main.customUploadFileName;
		else
			this.fileName = Main.DEFAULT_IMAGE_UPLOAD_FILENAME + '.' + Main.DEFAULT_IMAGE_UPLOAD_EXTENSION;
	}
	
	public FileUploader(String uploadString) throws IOException {
		this.bytes = uploadString.getBytes();

		if (Main.customUploadFileName != null && !Main.customUploadFileName.isBlank())
			this.fileName = Main.customUploadFileName;
		else
			this.fileName = Main.DEFAULT_TEXT_UPLOAD_FILENAME + '.' + Main.DEFAULT_TEXT_UPLOAD_EXTENSION;
	}
	
	public FileUploader(File uploadFile) throws IOException {
		this.bytes = Files.readAllBytes(uploadFile.toPath());

		if (Main.customUploadFileName != null && !Main.customUploadFileName.isBlank())
			this.fileName = Main.customUploadFileName;
		else
			this.fileName = uploadFile.getName();
	}
	
	public CloseableHttpResponse upload() throws Exception {
		MultipartEntityBuilder entityBuilder = MultipartEntityBuilder
				.create()
				.addBinaryBody(Main.formFileKeyName, bytes, ContentType.MULTIPART_FORM_DATA, fileName);
		
		for (String extra : Main.extraParams) {
			String[] splitTokens = extra.split("\\=", 2);
			
			entityBuilder.addTextBody(splitTokens[0], splitTokens[1]);
		}

		final CloseableHttpClient httpClient = HttpClients.createDefault();
		final HttpPost request = new HttpPost(Main.mediaURL);
		request.setEntity(entityBuilder.build());

		return httpClient.execute(request);
	}

}
