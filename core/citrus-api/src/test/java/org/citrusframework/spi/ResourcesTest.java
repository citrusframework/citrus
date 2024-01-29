package org.citrusframework.spi;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.Resources.ByteArrayResource;
import org.citrusframework.spi.Resources.ClasspathResource;
import org.citrusframework.spi.Resources.FileSystemResource;
import org.citrusframework.spi.Resources.UrlResource;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertThrows;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

public class ResourcesTest {

    private static URI baseUri;
    private static URI baseFolderUri;
    private static URI fileWithContentUri;
    private static URI fileWithoutContentUri;
    private static URI nonExistingFileUri;

    @BeforeClass
    public static void beforeClass() throws URISyntaxException {
        Resource resource = Resources.create("/ResourcesTest");
        baseUri = resource.getFile().getParentFile().toURI();
        baseFolderUri = resource.getFile().toURI();

        fileWithContentUri = new URI(baseFolderUri + "FileWithContent.json");
        fileWithoutContentUri = new URI(baseFolderUri + "FileWithoutContent.txt");
        nonExistingFileUri = new URI(baseFolderUri + "NonExistingFile.txt");
    }

    @Test
    public void urlNullDoesNotExistTest() {
        assertFalse(new UrlResource(null).exists());
    }

    @Test
    public void classpathResourceTest() {
        Resource withContentResource = Resources.create(
            baseUri.relativize(fileWithContentUri).getPath());
        assertTrue(withContentResource.exists());

        Resource withoutContentResource = Resources.create(
            baseUri.relativize(fileWithoutContentUri).getPath());
        assertTrue(withoutContentResource.exists());

        Resource nonExistingResource = Resources.create(
            baseUri.relativize(nonExistingFileUri).getPath());
        assertFalse(nonExistingResource.exists());
    }

    @Test
    public void byteArrayResourceTest() {
        Resource byteArrayResource = new ByteArrayResource(new byte[100]);
        assertEquals(byteArrayResource.getLocation(), "");
        assertTrue(byteArrayResource.exists());
        assertTrue(byteArrayResource.getInputStream() instanceof ByteArrayInputStream);
        assertThrows(UnsupportedOperationException.class, byteArrayResource::getFile);
    }

    @Test
    public void defaultFileSystemResourceTest() {
        Resource resource = Resources.create("/ResourcesTest");
        File resourceFolder = resource.getFile();

        assertTrue(resourceFolder.exists());
        assertTrue(resourceFolder.isDirectory());

        Resource withContentResource = Resources.create(fileWithContentUri.getPath());
        assertTrue(withContentResource instanceof FileSystemResource);
        assertTrue(withContentResource.exists());

        Resource withoutContentResource = Resources.create(fileWithoutContentUri.getPath());
        assertTrue(withoutContentResource instanceof FileSystemResource);
        assertTrue(withoutContentResource.exists());

        Resource nonExistingResource = Resources.create(nonExistingFileUri.getPath());
        assertTrue(nonExistingResource instanceof ClasspathResource);
        assertFalse(nonExistingResource.exists());
    }

    @Test
    public void fileSystemResourceTest() throws IOException {
        Resource resource = Resources.create("/ResourcesTest");
        File file = resource.getFile();

        assertTrue(file.exists());
        assertTrue(file.isDirectory());

        Resource withContentResource = Resources.create(fileWithContentUri.toString());
        assertTrue(withContentResource instanceof FileSystemResource);
        assertTrue(withContentResource.exists());
        assertEquals(fileWithContentUri, withContentResource.getURI());
        assertEquals(new File(fileWithContentUri).getPath(), withContentResource.getLocation());
        try (InputStream inputStream = withContentResource.getInputStream()) {
            assertNotNull(inputStream);
        }

        Resource withoutContentResource = Resources.create(fileWithoutContentUri.toString());
        assertTrue(withoutContentResource instanceof FileSystemResource);
        assertTrue(withoutContentResource.exists());
        try (InputStream inputStream = withoutContentResource.getInputStream()) {
            assertNotNull(inputStream);
        }

        Resource nonExistingResource = Resources.create(nonExistingFileUri.toString());
        assertTrue(nonExistingResource instanceof FileSystemResource);
        assertFalse(nonExistingResource.exists());
        assertThrows(CitrusRuntimeException.class, nonExistingResource::getInputStream);

        assertThrows(UnsupportedOperationException.class, () -> new FileSystemResource(new File(baseFolderUri)).getInputStream());
    }

    @Test
    public void urlResourceTest() throws MalformedURLException {
        Resource httpResource = Resources.create(Resources.HTTP_RESOURCE_PREFIX + "//host/context");
        assertTrue(httpResource instanceof UrlResource);

        URL withContentUrlMock = spy(fileWithContentUri.toURL());
        UrlResource withContentResource = new UrlResource(withContentUrlMock);
        assertTrue(withContentResource.exists());
        assertEquals(new File(fileWithContentUri), withContentResource.getFile());
        assertEquals(fileWithContentUri.toURL().toString(), withContentResource.getLocation());

        URL withoutContentUrlMock = spy(fileWithoutContentUri.toURL());
        UrlResource withoutContentResource = new UrlResource(withoutContentUrlMock);
        assertTrue(withoutContentResource.exists());
        assertEquals(new File(fileWithoutContentUri), withoutContentResource.getFile());

        URL nonExistingUrlMock = spy(nonExistingFileUri.toURL());
        UrlResource nonExistingResource = new UrlResource(nonExistingUrlMock);
        assertFalse(nonExistingResource.exists());
        assertEquals(new File(nonExistingFileUri), nonExistingResource.getFile());
    }

    @Test
    public void urlResourceExistsOnConnectionTest() throws IOException {
        URL withContentUrlMock = spy(new URL(Resources.HTTP_RESOURCE_PREFIX + "//host/context"));
        HttpURLConnection urlConnectionMock = mock(HttpURLConnection.class);
        doReturn(urlConnectionMock).when(withContentUrlMock).openConnection();
        doReturn(HttpURLConnection.HTTP_OK).when(urlConnectionMock).getResponseCode();
        assertTrue(new UrlResource(withContentUrlMock).exists());
    }


    @Test
    public void urlResourceNotExistsOnConnectionFailureTest() throws IOException {
        URL withContentUrlMock = spy(new URL(Resources.HTTP_RESOURCE_PREFIX + "//host/context"));
        HttpURLConnection urlConnectionMock = mock(HttpURLConnection.class);
        doReturn(urlConnectionMock).when(withContentUrlMock).openConnection();
        doReturn(HttpURLConnection.HTTP_INTERNAL_ERROR).when(urlConnectionMock).getResponseCode();
        assertFalse(new UrlResource(withContentUrlMock).exists());
    }

    @Test
    public void uriSyntaxThrowsCitrusRuntimeException()
        throws MalformedURLException, URISyntaxException {
        URL withContentUrlMock = spy(fileWithContentUri.toURL());
        doThrow(new URISyntaxException("xxxx", "Test Exception[uriSyntaxThrowsCitrusRuntimeException]"))
            .when(withContentUrlMock).toURI();
        UrlResource withContentResource = new UrlResource(withContentUrlMock);
        assertThrows(CitrusRuntimeException.class, withContentResource::exists);
    }

    @Test
    public void urlResourceNotExistsOnIOExceptionTest() throws IOException {
        URL withContentUrlMock = spy(new URL(Resources.HTTP_RESOURCE_PREFIX + "//host/context"));
        doThrow(new IOException("Test Exception[urlResourceNotExistsOnIoExceptionTest]"))
            .when(withContentUrlMock).openConnection();
        assertThrows(CitrusRuntimeException.class, () -> new UrlResource(withContentUrlMock).exists());
    }

    @Test
    public void urlResourceInputStreamTest() throws IOException {
        URL withContentUrlMock = spy(new URL(Resources.HTTP_RESOURCE_PREFIX + "//host/context"));
        HttpURLConnection urlConnectionMock = mock(HttpURLConnection.class);
        InputStream inputStreamMock = mock(InputStream.class);

        doReturn(urlConnectionMock).when(withContentUrlMock).openConnection();
        doReturn(inputStreamMock).when(urlConnectionMock).getInputStream();
        assertEquals(inputStreamMock, new UrlResource(withContentUrlMock).getInputStream());

        verify(urlConnectionMock).disconnect();
    }

    @Test
    public void urlResourceInputStreamIOExceptionTest() throws IOException {
        URL withContentUrlMock = spy(new URL(Resources.HTTP_RESOURCE_PREFIX + "//host/context"));
        HttpURLConnection urlConnectionMock = mock(HttpURLConnection.class);

        doReturn(urlConnectionMock).when(withContentUrlMock).openConnection();

        doThrow(new IOException("Test Exception[urlResourceInputStreamIOExceptionTest]"))
            .when(urlConnectionMock).getInputStream();

        assertThrows(CitrusRuntimeException.class, () -> new UrlResource(withContentUrlMock).getInputStream());

        verify(urlConnectionMock).disconnect();
    }
}
