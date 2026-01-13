/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.xwiki.pdfviewer.test.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.xwiki.flamingo.skin.test.po.AttachmentsPane;
import org.xwiki.flamingo.skin.test.po.AttachmentsViewPage;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.test.docker.junit5.ExtensionOverride;
import org.xwiki.test.docker.junit5.TestConfiguration;
import org.xwiki.test.docker.junit5.UITest;
import org.xwiki.test.ui.TestUtils;
import org.xwiki.test.ui.po.CreatePagePage;
import org.xwiki.test.ui.po.ViewPage;
import org.xwiki.test.ui.po.editor.EditPage;

import com.xwiki.pdfviewer.po.PDFViewerMacro;
import com.xwiki.pdfviewer.po.PDFViewerMacroPage;
import com.xwiki.pdfviewer.po.TabLayoutPDFMacro;
import com.xwiki.pdfviewer.po.TabLayoutPDFMacroPage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UITest(extensionOverrides = { @ExtensionOverride(extensionId = "com.google.code.findbugs:jsr305", overrides = {
    "features=com.google.code.findbugs:annotations" }),
    @ExtensionOverride(extensionId = "org.bouncycastle:bcprov-jdk18on", overrides = {
        "features=org.bouncycastle:bcprov-jdk15on" }),
    @ExtensionOverride(extensionId = "org.bouncycastle:bcpkix-jdk18on", overrides = {
        "features=org.bouncycastle:bcpkix-jdk15on" }),
    @ExtensionOverride(extensionId = "org.bouncycastle:bcmail-jdk18on", overrides = {
        "features=org.bouncycastle:bcmail-jdk15on" }) })

public class PDFViewerIT
{
    @BeforeAll
    void beforeAll(TestUtils testUtils)
    {
        testUtils.loginAsSuperAdmin();
    }

    @Test
    @Order(1)
    void pdfAttachedToCurrentPageTest(TestUtils setup, TestConfiguration testConfiguration)
    {
        createPage(setup, getMacroContent("pdfMacroContent.vm"), "pdfAttachedToCurrentPageTest");

        uploadFile("PDFTest.pdf", testConfiguration);
        PDFViewerMacroPage page = new PDFViewerMacroPage();
        page.reloadPage();

        assertEquals(3, page.getPDFViewerMacrosCount());

        PDFViewerMacro viewer0 = page.getPDFViewer(0);
        assertEquals("100%", viewer0.getWidth());
        assertEquals("1000px", viewer0.getHeight());
        assertEquals("PDF file for testing the pdf viewer macro.", viewer0.getText());

        PDFViewerMacro viewer1 = page.getPDFViewer(1);
        assertEquals("50%", viewer1.getWidth());
        assertEquals("500px", viewer1.getHeight());
        assertEquals("PDF file for testing the pdf viewer macro.", viewer1.getText());

        PDFViewerMacro viewer2 = page.getPDFViewer(2);
        assertEquals("100%", viewer2.getWidth());
        assertEquals("1000px", viewer2.getHeight());
        assertEquals("PDF file for testing the pdf viewer macro.", viewer2.getText());
    }

    @Test
    @Order(2)
    void pdfAttachedToAnotherPageTest(TestUtils setup, TestConfiguration testConfiguration) throws Exception
    {
        createPage(setup, "normal page with a pdf attached", "PageWithAttachedPDF");
        uploadFile("PDFTest.pdf", testConfiguration);

        createPage(setup, "{{pdfviewer file=\"PDFTest.pdf\" document=\"PDFViewerMacro.PageWithAttachedPDF\"/}}",
            "pdfAttachedToAnotherPageTest");

        PDFViewerMacroPage page = new PDFViewerMacroPage();

        assertEquals(1, page.getPDFViewerMacrosCount());
        PDFViewerMacro viewer0 = page.getPDFViewer(0);
        assertEquals("PDF file for testing the pdf viewer macro.", viewer0.getText());
    }

    @Test
    @Order(3)
    void pdfAttachedToTerminalPageTest(TestUtils setup, TestConfiguration testConfiguration) throws Exception
    {
        createTerminalPageWithPDFAttached(setup, testConfiguration);
        createPage(setup, "{{pdfviewer file=\"PDFTest.pdf\" document=\"PDFViewerMacro.TerminalPageWithPDF\"/}}",
            "pdfAttachedToTerminalPageTest");
        PDFViewerMacroPage page = new PDFViewerMacroPage();

        assertEquals(1, page.getPDFViewerMacrosCount());
        PDFViewerMacro viewer0 = page.getPDFViewer(0);
        assertEquals("PDF file for testing the pdf viewer macro.", viewer0.getText());
    }

    @Test
    @Order(4)
    void tabLayoutTest(TestUtils setup, TestConfiguration testConfiguration)
    {
        createPage(setup, getMacroContent("multiplePDFs.vm"), "tabLayoutTest");

        uploadFile("PDFTest-1.pdf", testConfiguration);
        uploadFile("PDFTest-2.pdf", testConfiguration);
        uploadFile("PDFTest-3.pdf", testConfiguration);

        TabLayoutPDFMacroPage page = new TabLayoutPDFMacroPage();
        page.reloadPage();
        assertEquals(4, page.getPDFViewerMacrosCount());
        TabLayoutPDFMacro viewer0 = page.getPDFViewer(0);
        assertEquals(3, viewer0.getTabs().size());
        assertEquals(Arrays.asList("PDFTest-1.pdf", "PDFTest-2.pdf", "PDFTest-3.pdf"), viewer0.getTabsNames());

        int activeTab = viewer0.getActiveTab();
        assertEquals(0, activeTab);
        assertEquals("PDFTest-1.pdf", viewer0.getTabName(activeTab));
        assertTrue(viewer0.getTabHref(activeTab).contains("PDFTest-1.pdf"));
        assertEquals("PDF file for testing the pdf viewer macro-1.", viewer0.getText());
        viewer0.clickTab(1);

        assertTrue(setup.getDriver().getCurrentUrl().contains("file=PDFTest-2.pdf"));
        TabLayoutPDFMacroPage page2 = new TabLayoutPDFMacroPage();
        TabLayoutPDFMacro viewer1 = page2.getPDFViewer(0);

        activeTab = viewer1.getActiveTab();
        assertEquals(1, activeTab);
        assertEquals("PDFTest-2.pdf", viewer1.getTabName(activeTab));
        assertTrue(viewer1.getTabHref(activeTab).contains("PDFTest-2.pdf"));
        assertEquals("PDF file for testing the pdf viewer macro-2.", viewer1.getText());
        viewer1.clickTab(2);

        assertTrue(setup.getDriver().getCurrentUrl().contains("file=PDFTest-3.pdf"));
        TabLayoutPDFMacroPage page3 = new TabLayoutPDFMacroPage();
        TabLayoutPDFMacro viewer2 = page3.getPDFViewer(0);

        activeTab = viewer2.getActiveTab();
        assertEquals(2, activeTab);
        assertEquals("PDFTest-3.pdf", viewer2.getTabName(activeTab));
        assertTrue(viewer2.getTabHref(activeTab).contains("PDFTest-3.pdf"));
        assertEquals("PDF file for testing the pdf viewer macro-3.", viewer2.getText());
    }

    @Test
    @Order(5)
    void externalPDFTest(TestUtils setup, TestConfiguration testConfiguration) throws Exception
    {
        createPage(setup, "normal page with a pdf attached", "NormalPageWithPDF");
        uploadFile("PDFTest.pdf", testConfiguration);

        String externalLink = getModifiedURL(setup.getDriver().getCurrentUrl(),
            "download/PDFViewerMacro/NormalPageWithPDF/PDFTest.pdf?rev=1.1");
        String content = "{{pdfviewer file=\"" + externalLink + "\"/}}";

        createPage(setup, content, "PageWithExternalPDFTest");
        PDFViewerMacroPage page = new PDFViewerMacroPage();
        PDFViewerMacro viewer0 = page.getPDFViewer(0);

        assertTrue(viewer0.getPdfUrl().contains("PDFTest.pdf"));
        assertTrue(viewer0.getPdfUrl().contains("PDFViewerMacro/NormalPageWithPDF"));
    }

    private void createTerminalPageWithPDFAttached(TestUtils setup, TestConfiguration testConfiguration)
        throws Exception
    {
        DocumentReference documentReference = new DocumentReference("xwiki", "PDFViewerMacro", "TerminalPageWithPDF");
        setup.deletePage(documentReference);

        ViewPage viewPage = setup.gotoPage(documentReference);
        CreatePagePage cpage = viewPage.createPage();
        cpage.setTerminalPage(true);
        cpage.clickCreate();
        EditPage ep = new EditPage();
        ep.clickSaveAndView();
        setup.attachFile(documentReference, "PDFTest.pdf", getClass().getResourceAsStream("/pdfmacro/PDFTest.pdf"),
            false);
    }

    private void uploadFile(String attachmentName, TestConfiguration testConfiguration)
    {
        String attachmentPath = new File(new File(testConfiguration.getBrowser().getTestResourcesPath(), "pdfmacro"),
            attachmentName).getAbsolutePath();
        AttachmentsPane sourceAttachmentsPane = new AttachmentsViewPage().openAttachmentsDocExtraPane();
        sourceAttachmentsPane.setFileToUpload(attachmentPath);
        sourceAttachmentsPane.waitForUploadToFinish(attachmentName);
    }

    private ViewPage createPage(TestUtils setup, String content, String pageName)
    {
        DocumentReference documentReference = new DocumentReference("xwiki", "PDFViewerMacro", pageName);
        return setup.createPage(documentReference, content);
    }

    private String getMacroContent(String filename)
    {
        try (InputStream inputStream = getClass().getResourceAsStream("/pdfmacro/" + filename)) {
            if (inputStream == null) {
                throw new RuntimeException("Failed to load " + filename + " from resources.");
            }

            return new BufferedReader(new InputStreamReader(inputStream)).lines()
                .filter(line -> !line.trim().startsWith("##")).collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read macro file: " + filename, e);
        }
    }

    private String getModifiedURL(String url, String newString)
    {
        int index = url.indexOf("bin/");
        if (index == -1) {
            return url;
        }
        return url.substring(0, index + 4) + newString;
    }
}
