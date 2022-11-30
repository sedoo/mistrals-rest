package fr.sedoo.mistrals.rest.service.v1_0;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.zeroturnaround.zip.ZipUtil;

import com.google.common.base.Strings;

import fr.sedoo.mistrals.rest.config.ApplicationConfig;
import fr.sedoo.mistrals.rest.dao.DataDirectory;
import fr.sedoo.mistrals.rest.dao.FileUtils;
import fr.sedoo.mistrals.rest.domain.DomainFilter;
import fr.sedoo.mistrals.rest.domain.DownloadInformations;
import fr.sedoo.mistrals.rest.domain.FileInfo;
import fr.sedoo.mistrals.rest.domain.OSEntry;
import fr.sedoo.mistrals.rest.domain.OSResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping(value = "/data/v1_0")
public class MistralsDataService {


	@Autowired
	ApplicationConfig config;
	
	@Autowired
	DataDirectory dataDirectory;

	private static final Logger LOG = LoggerFactory.getLogger(MistralsDataService.class);

	@RequestMapping(value = "/isalive", method = RequestMethod.GET)
	public String isalive() {
		return "yes";
	}
	
	@RequestMapping(value = "/toscript", method = RequestMethod.GET)
	@Produces(MediaType.TEXT_PLAIN)
	@Operation(summary= "Return the download command line")
	public String toScript(HttpServletRequest request,
			@Parameter(description = "id of the collection to be scripted") @RequestParam("collectionId") String collectionId,
			@Parameter(description = "filter that indicates the selected items") @RequestParam(required = false) String filter) {
		//String baseUrl = "https://" + request.getServerName() + request.getContextPath();
		String baseUrl = "https://api.sedoo.fr/sedoo-baobab-rest";
		LOG.debug("baseurl: " + baseUrl);
		String command = "";
		if (!Strings.isNullOrEmpty(filter)) {
			command = "curl -X GET " + baseUrl + "/data/v1_0/download?collectionId=" + collectionId
					+ "/&filter=" + filter + " --output dataset" + collectionId + ".zip";
		} else {
			command = "curl -X GET " + baseUrl + "/data/v1_0/download?collectionId=" + collectionId
					+ " --output dataset" + collectionId + ".zip";
		}

		return command;
	}
	
	private String getZipFileNameFromId(String id) {
		return "dataset-" + id + ".zip";
	}
	
	private List<String> getFileNameFromUuidAndYear(String collection, String filter, String folder) throws IOException {
		DomainFilter domain = new DomainFilter(filter);
		LOG.info("YEARS "+domain.getYears());
		File workDirectory = config.getMistralsRootFolder();
		File resource = new File(workDirectory, collection+folder);
		LOG.info("Path "+resource.getAbsolutePath());
		List<String> currentFile = new ArrayList<>();
		File [] listOfFiles = resource.listFiles();
		for (File file : listOfFiles) {
			for (String year : domain.getYears()) {
				if (file.getName().contains(year)) {
					String destinationFilePath = resource.getAbsolutePath().concat("/").concat(file.getName());
					
					currentFile.add(destinationFilePath);
				}else {
					System.out.println("Invalide folder");
				}
			}
		}
		return currentFile;
	}
	
	private List<String> getFolderFromUuidAndYear( String filter, String folder) {
		DomainFilter domain = new DomainFilter(filter);
		File workDirectory = config.getMistralsRootFolder();
		File resource = new File(workDirectory, folder);
		List<String> currentFile = new ArrayList<>();
		File [] listOfFiles = resource.listFiles();
		for (File file : listOfFiles) {
			for (String year : domain.getYears()) {
				if (file.isDirectory() && file.getName().contains(year)) {
					currentFile.add(file.getAbsolutePath());
				}
			}
		}
		return currentFile;
	}
	
	@RequestMapping(value = "/download", method = RequestMethod.GET)
	@Produces("application/zip")
	@Operation(summary= "Return files for the collection id")
	public byte[] download(HttpServletResponse response,
			@Parameter(description= "id of the collection to be downloaded") @RequestParam("collectionId") String collectionId,
			@Parameter(description = "filter indicates the selected items") @RequestParam(required = false) String filter) throws Exception {
		LOG.info("Starting download collection " + collectionId);

		String zipFileName = getZipFileNameFromId(collectionId);
		File workDirectory = config.getMistralsRootFolder();
		if (workDirectory.exists() == false) {
			workDirectory.mkdirs();
		}
		String folder = dataDirectory.getFolderFromUuid(collectionId);
		File requestFolder = new File(workDirectory, folder);
		if (requestFolder.exists() == false) {
			requestFolder.mkdirs();
		}
		File workDirectoryTmp = config.getTemporaryDownloadFolder();
		File zipFile = new File(workDirectoryTmp, zipFileName);
		
		
		LOG.info("Request folder  " + requestFolder.getAbsolutePath());
		

		ZipUtil.pack(requestFolder, zipFile);

		java.nio.file.Path p = zipFile.toPath();
		try {
			InputStream is = Files.newInputStream(p);
			byte[] result = IOUtils.toByteArray(is);

			response.addHeader("Content-Disposition", "attachment; filename=" + zipFileName);
			return result;
		}catch(Exception e) {
			LOG.error(ExceptionUtils.getStackTrace(e));
			throw new WebApplicationException(Response.Status.NOT_FOUND);
			
		}
	}
	
	@RequestMapping(value = "/downloadFolderYear", method = RequestMethod.GET)
	@Produces("application/zip")
	@Operation(summary = "Return files for the collection id")
	public byte[] downloadFolderYear(HttpServletResponse response,
			@Parameter(description= "id of the collection to be downloaded") @RequestParam("collectionId") String collectionId,
			@Parameter(description= "filter indicates the selected items") @RequestParam(required = false) String filter
			) throws Exception {
		LOG.info("Starting download collection " + collectionId);
		String zipFileName = getZipFileNameFromId(collectionId);
		File workDirectoryTmp = config.getTemporaryDownloadFolder();
		File zipFile = new File(workDirectoryTmp, zipFileName);
		String folder = dataDirectory.getFolderFromUuid(collectionId);
		List<String> fileNames = getFolderFromUuidAndYear( filter, folder);
		for (String file : fileNames) {
			File requestFolders = new File(file);
			ZipUtil.pack(requestFolders, zipFile);
		}
		java.nio.file.Path p = zipFile.toPath();
		try {
			InputStream is = Files.newInputStream(p);
			byte[] result = IOUtils.toByteArray(is);

			response.addHeader("Content-Disposition", "attachment; filename=" + zipFileName);
			return result;
		}catch(Exception e) {
			LOG.error(ExceptionUtils.getStackTrace(e));
			throw new WebApplicationException(Response.Status.NOT_FOUND);

		}
	}


	@RequestMapping(value = "/downloadYear", method = RequestMethod.GET)
	@Produces("application/zip")
	@Operation(summary = "Return files for the collection id")
	public void downloadFile (HttpServletResponse response,
			@Parameter(description = "id of the collection to be downloaded") @RequestParam("collectionId") String collectionId,
			@Parameter(description = "filter indicates the selected items") @RequestParam(required = true) String filter,
			@Parameter(description =  "folder indicates the selected items") @RequestParam(required = true) String folder) throws Exception {
		LOG.info("Starting download collection " + collectionId);
		String zipFileName = getZipFileNameFromId(collectionId);

		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition", "attachment;filename="+zipFileName);
		response.setStatus(HttpServletResponse.SC_OK);

		List<String> fileNames = getFileNameFromUuidAndYear(collectionId, filter, folder);
		
		
		LOG.info("file size " + fileNames.size());

		try (ZipOutputStream zippedOut = new ZipOutputStream(response.getOutputStream())) {
			for (String file : fileNames) {
				FileSystemResource resource = new FileSystemResource(file);

				ZipEntry e = new ZipEntry(resource.getFilename());
				// Configure the zip entry, the properties of the file
				e.setSize(resource.contentLength());
				e.setTime(System.currentTimeMillis());
				zippedOut.putNextEntry(e);
				// And the content of the resource:
				StreamUtils.copy(resource.getInputStream(), zippedOut);
				zippedOut.closeEntry();
			}
			zippedOut.finish();
		} catch (Exception e) {
			LOG.error(ExceptionUtils.getStackTrace(e));
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
	}



	
	@RequestMapping(value = "/download", method = RequestMethod.HEAD)
	@Produces("application/zip")
	@Operation(summary = "Return files for the collection id")
	public void downloadHead(HttpServletResponse response,
			@Parameter(description = "id of the collection to be downloaded") @RequestParam("collectionId") String collectionId) {
		response.addHeader("Content-Disposition", "attachment; filename=" + getZipFileNameFromId(collectionId));
	}

		
	public static long folderSize(File directory) {
	    long length = 0;
	    for (File file : directory.listFiles()) {
	        if (file.isFile())
	            length += file.length();
	        else
	            length += folderSize(file);
	    }
	    return length;
	}
	
	
	
	
	@RequestMapping(value = "/request1", method = RequestMethod.GET)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Return year file data")
	public OSResponse request1(
			@Parameter(description = "id of the collection to be downloaded") @RequestParam("collection") String collection/*,
			@Parameter(description = "folder indicates the selected items") @RequestParam(required = true) String folder*/) {

		OSResponse response = new OSResponse();
		List<OSEntry> entries = new ArrayList<>();
		response.setEntries(entries);

		try {
			Map<String, OSEntry> entriesMap = new HashMap<>();
			String folder = dataDirectory.getFolderFromUuid(collection);
			File resource = new File(config.getMistralsRootFolder(), folder);
			LOG.debug("FICHIER: "+ config.getMistralsRootFolder(), folder);
			File [] listOfFiles = resource.listFiles();
			for (File file : listOfFiles) {
				if (file.isDirectory()) {
					String year = FileUtils.getYear(file.getName());
					if (StringUtils.isNoneEmpty(year)) {
						OSEntry osEntry = entriesMap.get(year);
						if (osEntry == null) {
							osEntry = new OSEntry();
							osEntry.setDate(new GregorianCalendar(new Integer(year), 0, 1).getTime());
							osEntry.setTotalSize(file.length());
							entriesMap.put(year, osEntry);
						}
					}
				}
			}
			entries.addAll(entriesMap.values());
			Collections.sort(entries);

			return response;
		} catch (Exception e) {
			LOG.error(ExceptionUtils.getStackTrace(e));
			throw new RuntimeException();
		}

	}
	
	@RequestMapping(value = "/request", method = RequestMethod.GET)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Return statistics on the download")
	public OSResponse request(
			@Parameter(description = "id of the collection to be downloaded", required = true) @RequestParam("collection") String collection) {

		OSResponse response = new OSResponse();
		List<OSEntry> entries = new ArrayList<>();
		response.setEntries(entries);
		
		try {

			String folder = dataDirectory.getFolderFromUuid(collection);
			File resource = new File(config.getMistralsRootFolder(), folder);
			File [] listOfFiles = resource.listFiles();
			for (File file : listOfFiles) {
			if (file.isDirectory()) {
				int filecount = resource.list().length;
				DownloadInformations downloadInformations = new DownloadInformations(filecount, folderSize(resource));
				OSEntry entry = new OSEntry();
				entry.setFileNumber(downloadInformations.getDownloadFileNumber());
				entry.setTotalSize(downloadInformations.getDownloadSize());
				LOG.info("File number  " + entry.getFileNumber());
				LOG.info("Total size  " + entry.getTotalSize());
				entries.add(entry);
			}else if (file.isFile()) {
				int filecount = resource.list().length;
				DownloadInformations downloadInformations = new DownloadInformations(filecount, folderSize(resource));
				OSEntry entry = new OSEntry();
				entry.setFileNumber(downloadInformations.getDownloadFileNumber());
				entry.setTotalSize(downloadInformations.getDownloadSize());
				LOG.info("File number  " + entry.getFileNumber());
				LOG.info("Total size  " + entry.getTotalSize());
				entries.add(entry);
			}
			}


			return response;
		} catch (Exception e) {
			LOG.error(ExceptionUtils.getStackTrace(e));
			throw new RuntimeException();
		}

	}
	
	@RequestMapping(value = "/dataFiles", method = RequestMethod.GET)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Return year file data")
	public List<FileInfo> dataFiles(
			@Parameter(description = "id of the collection to be downloaded") @RequestParam("collection") String collection) {

		List<FileInfo> entries = new ArrayList<>();

		try {
			File resource = new File(config.getMistralsRootFolder(), collection);
			File [] listOfFiles = resource.listFiles();
			for (File file : listOfFiles) {
				FileInfo entry = new FileInfo();
				entry.setName(file.getName());
				entries.add(entry);
			}

			return entries;
		} catch (Exception e) {
			LOG.error(ExceptionUtils.getStackTrace(e));
			throw new RuntimeException();
		}

	}
	
	@RequestMapping(value = "/delete", method = RequestMethod.DELETE)
	@ResponseBody
	public void deleteFile(@RequestParam("fileName") String fileName,  @RequestParam("collection") String collection) {
		File resource = new File(config.getMistralsRootFolder(), collection);
		String file = resource.getAbsolutePath().concat("/").concat(fileName);
		try {
			Boolean result = Files.deleteIfExists(Paths.get(file));
			if (result) {
				System.out.println("File is deleted!");
			}
			else {
                System.out.println("Unable to delete the file.");
            }
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	@RequestMapping(value = "/deleteAll", method = RequestMethod.DELETE)
	@ResponseBody
	public void deleteAll(@RequestParam("collection") String collection) throws IOException {
		File resource = new File(config.getMistralsRootFolder(), collection);
		File [] listOfFiles = resource.listFiles();
		if (resource.exists() && listOfFiles != null) {
			for (File f : listOfFiles) {
				boolean result = f.delete();
				if (result) {
					System.out.println(f.getName()+" is deleted!");
				}else {
				    System.out.println("Unable to delete the file.");
				}
			}
		}
		
	}
	
	
	@RequestMapping(value = "/upload", method = RequestMethod.POST )
	@ResponseBody
	public List<FileInfo> uploadMultipleFiles (@RequestParam("collection") String collection, @RequestParam(value = "file") MultipartFile[] files){
		
		
		List<FileInfo> fileInfos = new ArrayList<>();
		
		
		File resource = new File(config.getMistralsRootFolder(), collection);
		
		if (resource.exists() == false) {
			resource.mkdirs();
		}
		
		for (MultipartFile multipleFile : files) {
			String filename = multipleFile.getOriginalFilename();
			
			String path = resource.getAbsolutePath().concat("/").concat(filename);
			File file = new File(path);
			
			try {
                if (file.createNewFile()) {
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        fos.write(multipleFile.getBytes());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            FileInfo fileInfo = new FileInfo();
            fileInfo.setName(filename);
            fileInfo.setUrl(path);
            fileInfos.add(fileInfo);
		}
		return fileInfos;
	}
	
	
	
	

	

	
}
