package assignment.navexample;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MakesFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class MakesFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private Context context;
    private ArrayAdapter arrayAdapter;
    public static ArrayList<String> carArray = new ArrayList<>();
    public static boolean makesInit = false;
    private ListView listView;
    public static final String MAKES_COPY = "com.Makes.copy";
    SQLiteDatabase myDatabase;

    public MakesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        myDatabase = getActivity().openOrCreateDatabase("ListDB", context.MODE_PRIVATE, null);
        myDatabase.execSQL("CREATE TABLE IF NOT EXISTS Makes(MakeName VARCHAR);");

        Bundle bundle = this.getArguments();
        if(bundle != null) {
            myDatabase.execSQL("INSERT INTO Makes VALUES('" + bundle.getString(MAKES_COPY) + "');");
        }
        return inflater.inflate(R.layout.fragment_makes, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = getView().getContext();

        listView = (ListView)getView().findViewById(R.id.makesListView);
        arrayAdapter = new ArrayAdapter(context, android.R.layout.simple_list_item_1, carArray);
        listView.setAdapter(arrayAdapter);

        populateList();

            FloatingActionButton fab = (FloatingActionButton) getView().findViewById(R.id.fab1);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                AlertDialog dialog;
                builder.setTitle("New Car Make");

                final EditText input = new EditText(context);

                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

                builder.setView(input);

                builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        myDatabase.execSQL("INSERT INTO Makes VALUES('" + input.getText().toString() + "');");
                        populateList();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
                dialog = builder.create();
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String item = listView.getItemAtPosition(position).toString();
                CharSequence options[] = new CharSequence[]{"Edit", "Delete", "Copy"};
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(item);

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                editItem(item);
                                break;
                            case 1:
                                deleteItem(item);
                                break;
                            default:
                                copyItem(item);
                                break;
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });
    }

    public void editItem(final String item){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Edit: " + item);

        final EditText input = new EditText(context);

        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setText(item);

        builder.setView(input);

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                myDatabase.execSQL("update Makes set MakeName = '" + input.getText().toString() + "' where MakeName = '" + item + "'");
                populateList();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    public void deleteItem(String item) {
        myDatabase.execSQL("delete from Makes where MakeName = '" + item + "'");
        populateList();
    }

    public void copyItem(String item){
        if (mListener != null) {
            mListener.onFragmentInteraction(item);
        }
    }

    public void sortList() {
        arrayAdapter.sort(new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return lhs.compareTo(rhs);
            }
        });
    }

    public void populateList(){

        carArray.clear();

        Cursor resultSet = myDatabase.rawQuery("Select * from Makes",null);
        resultSet.moveToFirst();
        while(resultSet.isAfterLast() == false){
            String carName = resultSet.getString(resultSet.getColumnIndex("MakeName"));
            carArray.add(carName);

            resultSet.moveToNext();
        }
        resultSet.close();
        arrayAdapter.notifyDataSetChanged();
        sortList();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(String item);
    }
}
