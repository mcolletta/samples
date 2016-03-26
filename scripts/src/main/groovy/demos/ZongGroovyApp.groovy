package demos

import groovy.swing.SwingBuilder
import groovy.beans.Bindable
import java.io.File;
import java.awt.image.BufferedImage;
import javax.swing.filechooser.FileFilter
import javax.swing.JFileChooser
import com.xenoage.zong.desktop.utils.JseZongPlatformUtils;
import com.xenoage.zong.core.Score;
import com.xenoage.zong.desktop.io.DocumentIO;
import com.xenoage.zong.desktop.io.musicxml.in.MusicXmlScoreDocFileInput;
import com.xenoage.zong.documents.ScoreDoc;
import com.xenoage.zong.layout.Layout;
import com.xenoage.zong.layout.frames.ScoreFrame;
import com.xenoage.zong.renderer.awt.AwtLayoutRenderer;

/**
 * Simple Zong Groovy App.
 * 
 * @author Mirco C.
 */

class ZongModel {
    @Bindable int page = 0
    @Bindable float zoom = 2
    @Bindable BufferedImage awtImage
    Layout layout
    
    ZongModel(appName = 'test') {
        JseZongPlatformUtils.init(appName);
    }

    public void loadScore(String filePath) {
        try {
            ScoreDoc scoreDoc = DocumentIO.read(new File(filePath), new MusicXmlScoreDocFileInput());
            loadScore(scoreDoc);
        }
        catch (Exception ex) {
            println ex.getMessage()
        }
    }
    
    public void loadScore(ScoreDoc doc) {
        ScoreDoc scoreDoc = doc
        layout = scoreDoc.getLayout()
        Score score = scoreDoc.getScore()
        layout.updateScoreLayouts(score)
        awtImage = renderLayout()
    }
    
    public BufferedImage renderLayout() {
        return AwtLayoutRenderer.paintToImage(layout, page, zoom);
    }
}

def model = new ZongModel()

def openScore = {    
    def dialog = new JFileChooser(dialogTitle: "Choose a MusicXml file",fileSelectionMode: JFileChooser.FILES_ONLY, 
                                  fileFilter: [getDescription: {-> "*.xml"}, accept:{file-> file ==~ /.*?\.xml/ || file.isDirectory() }] as FileFilter)
    def openResult = dialog.showOpenDialog()
    if (JFileChooser.APPROVE_OPTION == openResult) {
        model.loadScore(dialog.selectedFile.toString())
        swing.scoreImage.icon = swing.imageIcon(model.awtImage)
    }
}

def showAbout = {
     def pane = swing.optionPane(message:'Simple Zong App with Groovy')
     def dialog = pane.createDialog(frame, 'About')
     dialog.show()
}

swing = new SwingBuilder()
frame = swing.frame(title: 'Frame', size: [700, 700], show: true) {
     menuBar {
        menu(text:'File') {
            menuItem(text:'Open...', actionPerformed:openScore)
            menuItem(text: 'Exit', actionPerformed: { dispose() })
        }
        menu(text:'View') {
            menuItem(text:'Zoom in', actionPerformed:{ model.with {zoom++; awtImage = renderLayout() } })
            menuItem(text:'Zoom out', actionPerformed:{ model.with {zoom--; awtImage = renderLayout() } })
        }
        menu(text:'About') {
             menuItem(text:'About', actionPerformed: showAbout)
        }
    }
    borderLayout()
    vbox {
        hbox {
            button('<<', enabled: bind { model.page > 0 },  actionPerformed: { model.with {page--; awtImage = renderLayout() } })
            label(text: 'Page: ')
            label(text: bind(source: model, sourceProperty: 'page'))
            button('>>', enabled: bind { model.page < model.layout.pages.size()-1 }, actionPerformed: { model.with {page++; awtImage = renderLayout() } })
        }
        scrollPane() {
            label(id: 'scoreImage', icon:  bind(source: model, sourceProperty: 'awtImage', converter: { imageIcon(it) }))
        }
    }
}
frame.show() 