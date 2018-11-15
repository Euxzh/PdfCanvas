package net.example.pdfcanvas;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import net.example.freedrawview.FreeDrawView;
import net.example.freedrawview.HistoryPath;
import net.muststudio.pdfcanvas.R;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    PDFView PdfView;
    FreeDrawView DrawView;
    String fileName;
    int currentPage = 0;
    ArrayList<ArrayList<HistoryPath>> paths;

    String PDFScanPath;

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<Chapter>> listDataChild;
    List<String> PDFScanResult;

    int CourseEditingId = -1;
    int ChapterEditingId = -1;
    int PDFSelectedId = -1;

    public enum StateEnums {
        ListShowing,
        Editing,
        PDFShowing,
    }

    protected StateEnums currentStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);

        PDFScanPath = Environment.getExternalStorageDirectory() + "/PDFs";
        loadListView();
    }

    private void loadListView() {
        currentStatus = StateEnums.ListShowing;
        setContentView(R.layout.list_view);

        // get the listview
        expListView = (ExpandableListView) findViewById(R.id.project_selection_list_view);

        // preparing list data
        loadList();

        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);

        // setting list adapter
        expListView.setAdapter(listAdapter);

        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                if (listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).fileName.length() > 0)
                    loadPDF(listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).fileName);
                return false;
            }
        });

        ((Button)findViewById(R.id.edit_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                beginEdit();
            }
        });
    }

    private void loadList() {
        File file = new File(Environment.getExternalStorageDirectory() + "/PDFs/pdfCanvas.list");
        try {
            FileInputStream fos = new FileInputStream(file);
            ObjectInputStream oos = new ObjectInputStream(fos);
            listDataHeader = (ArrayList<String>) oos.readObject();
            listDataChild = (HashMap) oos.readObject();
            oos.close();
            fos.close();
            if (listDataHeader == null)
                listDataHeader = new ArrayList<>();
            if (listDataChild == null)
                listDataChild = new HashMap<>();
        } catch (IOException ioe) {
            listDataHeader = new ArrayList<>();
            listDataChild = new HashMap<>();
            ioe.printStackTrace();
        } catch (ClassNotFoundException cnfe) {
            listDataHeader = new ArrayList<>();
            listDataChild = new HashMap<>();
            cnfe.printStackTrace();
        }
    }

    private void saveList() {
        File file = new File(Environment.getExternalStorageDirectory() + "/PDFs/pdfCanvas.list");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(listDataHeader);
            oos.writeObject(listDataChild);
            oos.close();
            fos.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK)
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (currentStatus) {
                    case ListShowing:
                        return super.onKeyDown(keyCode, event);
                    case Editing:
                        loadListView();
                        break;
                    case PDFShowing:
                        loadListView();
                        break;
                }
                return true;
            }
        return super.onKeyDown(keyCode, event);
    }

    protected void beginEdit() {
        currentStatus = StateEnums.Editing;
        setContentView(R.layout.edit_view);

        // get the listview
        expListView = (ExpandableListView) findViewById(R.id.course_list_preview);

        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);

        // setting list adapter
        expListView.setAdapter(listAdapter);

        expListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                findViewById(R.id.default_panel).setVisibility(View.GONE);
                findViewById(R.id.manage_chapter_panel).setVisibility(View.GONE);
                findViewById(R.id.manage_course_panel).setVisibility(View.VISIBLE);
                CourseEditingId = groupPosition;
                ((TextView) findViewById(R.id.manage_course_rename_edit)).setText(listDataHeader.get(groupPosition));
                return false;
            }
        });
        expListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener(){
            @Override
            public void onGroupCollapse(int groupPosition) {
                findViewById(R.id.manage_course_panel).setVisibility(View.GONE);
                findViewById(R.id.manage_chapter_panel).setVisibility(View.GONE);
                findViewById(R.id.default_panel).setVisibility(View.VISIBLE);
            }
        });
        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                findViewById(R.id.default_panel).setVisibility(View.GONE);
                findViewById(R.id.manage_chapter_panel).setVisibility(View.VISIBLE);
                findViewById(R.id.manage_course_panel).setVisibility(View.GONE);
                CourseEditingId = groupPosition;
                ChapterEditingId = childPosition;
                ((TextView) findViewById(R.id.manage_chapter_rename_edit)).setText(listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).showenText);
                return false;
                //  loadPDF(listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).fileName);
            }
        });

        File dir = new File(PDFScanPath);
        dir.mkdirs();
        File[] filelist = dir.listFiles();
        PDFScanResult = new ArrayList<String>();
        for (File thisFile : filelist) {
            String filename = thisFile.getName();
            String filenameArray[] = filename.split("\\.");
            String extension = filenameArray[filenameArray.length - 1];
            if (extension.equalsIgnoreCase("pdf"))
                PDFScanResult.add(filename);
        }
        ListView list = (ListView) findViewById(R.id.pdf_list);
        list.setAdapter(new ArrayAdapter<String>(this, R.layout.simple_list_item, PDFScanResult));
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PDFSelectedId = position;
                ((TextView) findViewById(R.id.pdf_name_to_bind)).setText(PDFScanResult.get(PDFSelectedId));
            }
        });

        ((Button) findViewById(R.id.discard_list_changes_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If you want a confirm box you should add it here.
                loadListView();
            }
        });
        ((Button) findViewById(R.id.save_list_changes_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveList();
                loadListView();
            }
        });
        View.OnClickListener add_course_listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String defaultName = "New Course";
                String finalName = defaultName;
                int counter = 2;
                while (listDataHeader.indexOf(finalName) != -1)
                    finalName = defaultName + (counter++);
                listDataHeader.add(finalName);
                listDataChild.put(finalName, new ArrayList<Chapter>());
                listAdapter.notifyDataSetInvalidated();
            }
        };
        ((Button) findViewById(R.id.add_course)).setOnClickListener(add_course_listener);
        ((Button) findViewById(R.id.add_course_2)).setOnClickListener(add_course_listener);
        ((Button) findViewById(R.id.rename_source_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oriName = listDataHeader.get(CourseEditingId);
                String defaultName = ((EditText) findViewById(R.id.manage_course_rename_edit)).getText().toString();
                if (defaultName.length() == 0)
                    return;
                if (defaultName.equals(oriName))
                    return;
                String newName = defaultName;
                int counter = 2;
                while (listDataHeader.indexOf(newName) != -1)
                    newName = defaultName + (counter++);

                List<Chapter> temp = listDataChild.get(oriName);
                listDataChild.remove(oriName);

                listDataHeader.set(CourseEditingId, newName);
                listDataChild.put(newName, temp);

                listAdapter.notifyDataSetInvalidated();
            }
        });
        ((Button) findViewById(R.id.delete_course)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oriName = listDataHeader.get(CourseEditingId);

                listDataChild.remove(oriName);
                listDataHeader.remove(CourseEditingId);

                findViewById(R.id.default_panel).setVisibility(View.VISIBLE);
                findViewById(R.id.manage_chapter_panel).setVisibility(View.GONE);
                findViewById(R.id.manage_course_panel).setVisibility(View.GONE);

                listAdapter.notifyDataSetInvalidated();
            }
        });
        View.OnClickListener add_chapter_listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String defaultName = "New Chapter";
                ;
                listDataChild.get(listDataHeader.get(CourseEditingId)).add(new Chapter(defaultName, ""));
                listAdapter.notifyDataSetInvalidated();
            }
        };
        ((Button) findViewById(R.id.add_chapter)).setOnClickListener(add_chapter_listener);
        ((Button) findViewById(R.id.add_chapter_2)).setOnClickListener(add_chapter_listener);
        ((Button) findViewById(R.id.rename_chapter_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String defaultName = ((EditText) findViewById(R.id.manage_chapter_rename_edit)).getText().toString();
                if (defaultName.length() == 0)
                    return;
                listDataChild.get(listDataHeader.get(CourseEditingId)).get(ChapterEditingId).showenText = defaultName;

                listAdapter.notifyDataSetInvalidated();
            }
        });

        ((Button) findViewById(R.id.delete_chapter)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oriName = listDataHeader.get(CourseEditingId);

                listDataChild.get(listDataHeader.get(CourseEditingId)).remove(ChapterEditingId);

                findViewById(R.id.default_panel).setVisibility(View.VISIBLE);
                findViewById(R.id.manage_chapter_panel).setVisibility(View.GONE);
                findViewById(R.id.manage_course_panel).setVisibility(View.GONE);

                listAdapter.notifyDataSetInvalidated();
            }
        });
        ((Button) findViewById(R.id.bind_pdf)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PDFSelectedId == -1)
                    return;
                listDataChild.get(listDataHeader.get(CourseEditingId)).get(ChapterEditingId).fileName = PDFScanPath + "/" + PDFScanResult.get(PDFSelectedId);

                listAdapter.notifyDataSetInvalidated();
            }
        });
    }

    protected void loadPDF(String pdfName) {
        fileName = pdfName;
        currentPage = 0;
        currentStatus = StateEnums.PDFShowing;
        setContentView(R.layout.activity_main);
        ((Button) findViewById(R.id.save_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                while (paths.size() < PdfView.getPageCount())
                    paths.add(null);
                paths.set(currentPage, DrawView.getPathsClone());
                Toast.makeText(MainActivity.this, R.string.save_in_process, Toast.LENGTH_SHORT).show();
                AsyncTask<ArrayList<ArrayList<HistoryPath>>, Void, Boolean> backgroundTask = new AsyncTask<ArrayList<ArrayList<HistoryPath>>, Void, Boolean>() {
                    @Override
                    protected Boolean doInBackground(ArrayList<ArrayList<HistoryPath>>... params) {
                        File file = new File(fileName + ".paths");
                        try {
                            FileOutputStream fos = new FileOutputStream(file);
                            ObjectOutputStream oos = new ObjectOutputStream(fos);
                            oos.writeObject(params[0]);
                            oos.close();
                            fos.close();
                            return true;
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        }
                        return false;
                    }

                    /** The system calls this to perform work in the UI thread and delivers
                     * the result from doInBackground() */
                    protected void onPostExecute(Boolean result) {
                        if (result) {
                            Toast.makeText(MainActivity.this, R.string.save_success, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, R.string.save_fail, Toast.LENGTH_SHORT).show();
                        }
                        findViewById(R.id.save_button).setEnabled(true);
                        DrawView.setEnabled(true);
                    }
                };
                findViewById(R.id.save_button).setEnabled(false);
                DrawView.setEnabled(false);
                backgroundTask.execute(paths);
            }
        });
        ((Button) findViewById(R.id.load_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = new File(fileName + ".paths");
                try {
                    FileInputStream fis = new FileInputStream(file);
                    ObjectInputStream ois = new ObjectInputStream(fis);
                    paths = (ArrayList) ois.readObject();
                    ois.close();
                    fis.close();
                    while (paths.size() < PdfView.getPageCount())
                        paths.add(null);
                    DrawView.restorePaths(paths.get(currentPage));
                    DrawView.invalidate();
                } catch (IOException ioe) {
                    Toast.makeText(MainActivity.this, R.string.load_fail, Toast.LENGTH_SHORT).show();
                    ioe.printStackTrace();
                } catch (ClassNotFoundException c) {
                    System.out.println("Class not found");
                    c.printStackTrace();
                }
            }
        });
        ((Button) findViewById(R.id.next_page_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                while (paths.size() < PdfView.getPageCount())
                    paths.add(null);
                if (currentPage + 1 >= PdfView.getPageCount())
                    Toast.makeText(MainActivity.this, R.string.next_page_error, Toast.LENGTH_SHORT).show();
                else {
                    paths.set(currentPage, DrawView.getPathsClone());
                    PdfView.jumpTo(currentPage += 1);
                    DrawView.restorePaths(paths.get(currentPage));
                    DrawView.invalidate();
                }
            }
        });
        ((Button) findViewById(R.id.last_page_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                while (paths.size() < PdfView.getPageCount())
                    paths.add(null);
                if (currentPage - 1 < 0)
                    Toast.makeText(MainActivity.this, R.string.last_page_error, Toast.LENGTH_SHORT).show();
                else {
                    paths.set(currentPage, DrawView.getPathsClone());
                    PdfView.jumpTo(currentPage -= 1);
                    DrawView.restorePaths(paths.get(currentPage));
                    DrawView.invalidate();
                }
            }
        });
        ((Button) findViewById(R.id.free_draw_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawView.currentState = FreeDrawView.DrawState.FreeDraw;
            }
        });
        ((Button) findViewById(R.id.line_draw_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawView.currentState = FreeDrawView.DrawState.LineDraw;
            }
        });
        ((Button) findViewById(R.id.circle_draw_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawView.currentState = FreeDrawView.DrawState.CircleDraw;
            }
        });


        ((Button) findViewById(R.id.black_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawView.setPaintColor(getResources().getColor(R.color.black));
            }
        });
        ((Button) findViewById(R.id.red_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawView.setPaintColor(getResources().getColor(R.color.red));
            }
        });
        ((Button) findViewById(R.id.blue_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawView.setPaintColor(getResources().getColor(R.color.blue));
            }
        });
        ((Button) findViewById(R.id.purple_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawView.setPaintColor(getResources().getColor(R.color.purple));
            }
        });
        ((Button) findViewById(R.id.undo_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DrawView.getUndoCount() > 0)
                    DrawView.undoLast();
                else
                    Toast.makeText(MainActivity.this, R.string.undo_fail_text, Toast.LENGTH_SHORT).show();
            }
        });
        ((Button) findViewById(R.id.redo_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DrawView.getRedoCount() > 0)
                    DrawView.redoLast();
                else
                    Toast.makeText(MainActivity.this, R.string.redo_fail_text, Toast.LENGTH_SHORT).show();
            }
        });
        ((Button) findViewById(R.id.discard_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawView.undoAll();
            }
        });
        ((SeekBar) findViewById(R.id.seek_bar_width)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    DrawView.setPaintWidthDp(progress + 4);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        PdfView = (PDFView) findViewById(R.id.pdfView);
        ((TextView) findViewById(R.id.textViewDebug)).setText(fileName);
        PdfView.fromFile(new File(fileName))
                .enableSwipe(false)
                .load();
        paths = new ArrayList<ArrayList<HistoryPath>>();
        DrawView = (FreeDrawView) findViewById(R.id.drawView);
    }

}
