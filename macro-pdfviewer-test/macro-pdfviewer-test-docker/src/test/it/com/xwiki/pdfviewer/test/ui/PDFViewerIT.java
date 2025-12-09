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
import org.xwiki.test.ui.po.ViewPage;

import com.xwiki.pdfviewer.po.PDFViewerMacroPage;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    void createPDF(TestUtils setup, TestConfiguration testConfiguration)
    {
        createPage(setup, getMacroContent("pdfMacroContent.vm"), "PageWithUploadedPDF");

        uploadFile("PDFTest.pdf", testConfiguration);
        PDFViewerMacroPage page = new PDFViewerMacroPage();
        page.reloadPage();

        //setup.getDriver().waitUntilCondition(driver->(page.getPDFViewerMacrosCount())==4);
        assertEquals(4, page.getPDFViewerMacrosCount(), 60000);
        setup.getDriver().waitUntilPageIsReloaded();
//        PDFViewerMacro viewer0 = page.getPDFViewer(0);
//        PDFViewerMacro viewer1 = page.getPDFViewer(1);
//        PDFViewerMacro viewer2 = page.getPDFViewer(2);
//        PDFViewerMacro viewer3 = page.getPDFViewer(3);

    }

    @Test
    @Order(2)
    void createPageWithMultiplePDFs(TestUtils setup, TestConfiguration testConfiguration)
    {
        createPage(setup, getMacroContent("multiplePDFs.vm"), "PageWithMultiplePDFs");

        uploadFile("PDFTest.pdf", testConfiguration);
        uploadFile("PDFTest-1.pdf", testConfiguration);
        uploadFile("PDFTest-2.pdf", testConfiguration);
        uploadFile("PDFTest-3.pdf", testConfiguration);
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
}