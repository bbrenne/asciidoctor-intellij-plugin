package org.asciidoc.intellij.pasteProvider;

import com.intellij.ide.PasteProvider;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.psi.PsiFile;
import org.asciidoc.intellij.actions.asciidoc.PasteImageAction;
import org.asciidoc.intellij.file.AsciiDocFileType;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.DataFlavor;
import java.io.File;

public class AsciiDocPasteProvider implements PasteProvider {
  @Override
  public void performPaste(@NotNull DataContext dataContext) {
    executeAction(PasteImageAction.ID, dataContext);
  }

  private void executeAction(@NotNull String actionId, @NotNull DataContext dataContext) {
    final Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
    if (editor == null) {
      return;
    }
    AnAction action = ActionManager.getInstance().getAction(actionId);
    if (action != null) {
      action.actionPerformed(AnActionEvent.createFromDataContext(ActionPlaces.UNKNOWN, null, dataContext));
    }
  }

  /**
   * Find out if this one should handle paste.
   * Will not be called for files copied externally (for example from Windows explorer), but will be called for files copied from IntelliJ.
   */
  @Override
  public boolean isPastePossible(@NotNull DataContext dataContext) {
    final Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
    final PsiFile file = CommonDataKeys.PSI_FILE.getData(dataContext);
    if (editor == null) {
      return false;
    }
    if (file == null) {
      return false;
    }
    if (file.getFileType() != AsciiDocFileType.INSTANCE) {
      return false;
    }
    CopyPasteManager manager = CopyPasteManager.getInstance();
    if (manager.areDataFlavorsAvailable(DataFlavor.imageFlavor)) {
      return true;
    }
    if (manager.areDataFlavorsAvailable(DataFlavor.javaFileListFlavor)) {
      java.util.List<File> fileList = manager.getContents(DataFlavor.javaFileListFlavor);
      if (fileList != null) {
        for (File f : fileList) {
          String name = f.getName().toLowerCase();
          if (name.endsWith(".png") || name.endsWith(".svg") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".gif")) {
            return true;
          }
        }
      }
    }
    return false;
  }

  @Override
  public boolean isPasteEnabled(@NotNull DataContext dataContext) {
    return isPastePossible(dataContext);
  }
}
