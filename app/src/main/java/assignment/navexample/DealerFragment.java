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
public class DealerFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private Context dealerContext;
    private ArrayAdapter dealerAdapter;
    public static ArrayList<String> dealerArray = new ArrayList<>();
    public static boolean dealerInit = false;
    private ListView dealerListView;
    public static final String DEALER_COPY = "com.Dealers.copy";
    SQLiteDatabase myDatabase;

    public DealerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        myDatabase = getActivity().openOrCreateDatabase("ListDB", getActivity().MODE_PRIVATE, null);
        myDatabase.execSQL("CREATE TABLE IF NOT EXISTS Dealers(DealerName VARCHAR);");

        Bundle bundle = this.getArguments();
        if(bundle != null) {
            myDatabase.execSQL("INSERT INTO Dealers VALUES('" + bundle.getString(DEALER_COPY) + "');");
        }

        return inflater.inflate(R.layout.fragment_dealer, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dealerContext = getView().getContext();

        dealerListView = (ListView)getView().findViewById(R.id.dealerListView);
        dealerAdapter = new ArrayAdapter(dealerContext, android.R.layout.simple_list_item_1, dealerArray);
        dealerListView.setAdapter(dealerAdapter);

        populateList();

        FloatingActionButton fab = (FloatingActionButton) getView().findViewById(R.id.fab2);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(dealerContext);
                AlertDialog dialog;
                builder.setTitle("New Dealership");

                final EditText input = new EditText(dealerContext);

                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

                builder.setView(input);

                builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        myDatabase.execSQL("INSERT INTO Dealers VALUES('" + input.getText().toString() + "');");
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

        dealerListView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String item = dealerListView.getItemAtPosition(position).toString();
                CharSequence options[] = new CharSequence[]{"Edit", "Delete", "Copy"};
                AlertDialog.Builder builder = new AlertDialog.Builder(dealerContext);
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

        final EditText input = new EditText(dealerContext);

        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setText(item);

        builder.setView(input);

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                myDatabase.execSQL("update Dealers set DealerName = '" + input.getText().toString() + "' where DealerName = '" + item + "'");
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
        myDatabase.execSQL("delete from Dealers where DealerName = '" + item + "'");
        populateList();
    }

    public void copyItem(String item){
        if (mListener != null) {
            mListener.onFragmentInteraction(item);
        }
    }

    public void sortList() {
        dealerAdapter.sort(new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return lhs.compareTo(rhs);
            }
        });
    }

    public void populateList(){

        dealerArray.clear();

        Cursor resultSet = myDatabase.rawQuery("Select * from Dealers",null);
        resultSet.moveToFirst();
        while(resultSet.isAfterLast() == false){
            String dealerName = resultSet.getString(resultSet.getColumnIndex("DealerName"));
            dealerArray.add(dealerName);

            resultSet.moveToNext();
        }
        resultSet.close();
        dealerAdapter.notifyDataSetChanged();
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
