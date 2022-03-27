package export_to_image.handlers;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;


public class SampleHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		IWorkbenchPart workBenchPart = window.getActivePage().getActivePart();
		
		// get content
		IFile file = (IFile) workBenchPart.getSite().getPage().getActiveEditor().getEditorInput().getAdapter(IFile.class);
		InputStream inputStream = null;
		try {
			inputStream = file.getContents();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String content = new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining("\n"));
		// save dialog
		FileDialog fd = new FileDialog(window.getShell(), SWT.SAVE);
		String[] extensions = { ".png" };
		fd.setFilterExtensions(extensions);
		String where = fd.open();
		if (where != null) {
			generate(content, where);
		}
		return null;
	}
	
	public void generate(String text, String path) {
		BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        Font font = new Font("Consolas", Font.PLAIN, 48);
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        int width = fm.stringWidth(text);
        
        int tempWidth = 0;
        for (String line : text.split("\n")) {
			if (fm.stringWidth(line + "       ") > tempWidth) {
				tempWidth = fm.stringWidth(line + "       ");
			}
		}
        int height = 0;
        int padding = 45;
        g2d.dispose();

        for (String line : text.split("\n")) {
			height += fm.getHeight();
		}
        height += padding;
        width += padding;

        img = new BufferedImage(tempWidth, height, BufferedImage.TYPE_INT_ARGB);
        g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2d.setFont(font);
        fm = g2d.getFontMetrics();
        g2d.fillRect(0, 0, tempWidth, height);
        g2d.setColor(Color.BLACK);
        plotToImg(g2d, text, 0, 0, padding);
        g2d.dispose();
        try {
        	File file = new File(path);
            ImageIO.write(img, "png", file);
            System.out.println("Success");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

	}
	public static void keywordDetector(Graphics2D g, String text, int x, int y) {
		if(text.contains("//") || text.contains("/*") || text.contains("*/"))
		{
			g.setColor(Color.gray);
			g.drawString(text, x, y);
	        g.setColor(Color.BLACK);
		}
		else
		{
			if (text.contains(" ")) {
				for (String word : text.split(" ")) {
					g.setColor(Color.BLACK);
					if (javax.lang.model.SourceVersion.isKeyword(word)) g.setColor(new Color(127, 0, 85));
					g.drawString(word, x, y);
			        g.setColor(Color.BLACK);
					x += g.getFontMetrics().stringWidth(word + " ");
				}
			}
			else
			{
		        g.setColor(Color.BLACK);
				g.drawString(text, x, y);
			}			
		}
	}
	public static void plotToImg(Graphics2D g, String text, int x, int y, int padding) {
		y += g.getFontMetrics().getHeight();
		x += padding;
		for (String line : text.split("\n")) {
			int thisY = y;
			if (line.contains("\t")) {
				int thisX = x;
				for (String ll : line.split("\t")) {
					keywordDetector(g, ll, thisX, thisY);
					FontMetrics fm = g.getFontMetrics();
					thisX += fm.stringWidth(ll) + 70;
				}
			}
			else {
				keywordDetector(g, line, x, thisY);
			}
			y += g.getFontMetrics().getHeight();
		}
	}
}
