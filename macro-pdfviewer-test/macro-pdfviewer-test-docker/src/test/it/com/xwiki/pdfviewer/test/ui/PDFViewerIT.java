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

import java.io.File;

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

@UITest(
    extensionOverrides = {
        @ExtensionOverride(
            extensionId = "com.google.code.findbugs:jsr305",
            overrides = {
                "features=com.google.code.findbugs:annotations"
            }),
        @ExtensionOverride(
            extensionId = "org.bouncycastle:bcprov-jdk18on",
            overrides = {
                "features=org.bouncycastle:bcprov-jdk15on"
            }
        ),
        @ExtensionOverride(
            extensionId = "org.bouncycastle:bcpkix-jdk18on",
            overrides = {
                "features=org.bouncycastle:bcpkix-jdk15on"
            }
        ),
        @ExtensionOverride(
            extensionId = "org.bouncycastle:bcmail-jdk18on",
            overrides = {
                "features=org.bouncycastle:bcmail-jdk15on"
            }
        )
    })
public class PDFViewerIT
{
    @BeforeAll
    void beforeAll(TestUtils testUtils)
    {
        testUtils.loginAsSuperAdmin();
    }

    @Test
    @Order(1)
    void createPDF(TestUtils setup,TestConfiguration testConfiguration)
    {
        createPage( setup,"{{pdfviewer file=\"PDFTest.pdf\"/}}", "PageWithUploadedPDF");
        uploadFile("PDFTest.pdf",testConfiguration);

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

}