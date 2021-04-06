package de.sirjofri.fingerlist;

import android.app.*;
import android.os.*;
import android.widget.*;
import java.util.*;
import android.view.*;
import android.content.*;
import android.widget.AdapterView.*;
import java.io.*;

public class MainActivity extends Activity 
{
	ListView fingerlistview;
	LinkedList<FingerEntry> fingerlist;
	FingerAdapter adapter;
	ReadWriter rw;
	MainActivity self;
	TextView hint;
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		self = this;
		
	//	findViewById(android.R.drawable.ic_menu_
	
		hint = findViewById(R.id.hint);
		
		adapter = new FingerAdapter(this);
		rw = new ReadWriter(getApplicationContext(), adapter, this);
		fingerlist = rw.load();
		
		fingerlistview = findViewById(R.id.fingerlist);
		adapter.setList(fingerlist);
		fingerlistview.setAdapter(adapter);
		
		fingerlistview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
			{
				manageItem(fingerlist.get(pos));
			}
		});
		
		hint.setVisibility(fingerlist.size() == 0 ? View.VISIBLE : View.GONE);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.layout.menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	public void notifyDataChanged()
	{
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				adapter.notifyDataSetChanged();
			}
		});
	}
	
	public void manageItem(final FingerEntry toReplace)
	{
		LayoutInflater inflater = getLayoutInflater();
		final View dview = inflater.inflate(R.layout.dialogmanage, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Manage Entry")
			.setView(dview);
		if (toReplace != null)
			((EditText)dview.findViewById(R.id.entry)).setText(toReplace.getAddress());
		builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface d, int id)
				{
					EditText e = dview.findViewById(R.id.entry);
					if (toReplace != null)
						fingerlist.remove(toReplace);
					fingerlist.add(new FingerEntry(e.getText().toString(), adapter, self));
					adapter.notifyDataSetChanged();
					rw.save(fingerlist);
					hint.setVisibility(fingerlist.size() == 0 ? View.VISIBLE : View.GONE);
				}
			});
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface d, int id)
				{
					d.cancel();
				}
			});
		if (toReplace != null)
		builder.setNeutralButton(R.string.delete, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface d, int id)
				{
					fingerlist.remove(toReplace);
					adapter.notifyDataSetChanged();
					rw.save(fingerlist);
					hint.setVisibility(fingerlist.size() == 0 ? View.VISIBLE : View.GONE);
				}
			});
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	public void reloadItems()
	{
		Iterator i = fingerlist.iterator();
		while (i.hasNext())
			((FingerEntry)i.next()).load();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId()) {
		case R.id.addbutton:
			manageItem(null);
			break;
		case R.id.updatebutton:
			reloadItems();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}

class FingerAdapter extends BaseAdapter
{
	Context ctxt;
	List<FingerEntry> list;
	
	public FingerAdapter(Context context, List<FingerEntry> list)
	{
		this.ctxt = context;
		this.list = list;
	}
	
	public FingerAdapter(Context c)
	{
		ctxt = c;
	}
	
	public void setList(LinkedList<FingerEntry> ll)
	{
		list = ll;
	}
	
	@Override
	public long getItemId(int p1)
	{
		// TODO: Implement this method
		return 0;
	}

	@Override
	public Object getItem(int pos)
	{
		return list.get(pos);
	}

	@Override
	public View getView(int pos, View convertView, ViewGroup parent)
	{
		LayoutInflater inflater = (LayoutInflater) ctxt.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View viewRow = inflater.inflate(R.layout.fingerrow, parent, false);
		TextView label = (TextView) viewRow.findViewById(R.id.label);
		TextView content = (TextView) viewRow.findViewById(R.id.content);
		label.setText(list.get(pos).getAddress());
		content.setText(list.get(pos).content);
		return viewRow;
	}

	@Override
	public int getCount()
	{
		return list.size();
	}
}

class ReadWriter
{
	private String filename = "fingerlist";
	private Context context;
	FingerAdapter adapter;
	MainActivity activity;
	
	public ReadWriter(Context c, FingerAdapter f, MainActivity a)
	{
		context = c;
		adapter = f;
		activity = a;
	}
	
	public byte[] fString(LinkedList<FingerEntry> ll)
	{
		Iterator i = ll.iterator();
		String s = new String();
		while (i.hasNext()) {
			s += ((FingerEntry)i.next()).getAddress() + "\n";
		}
		return s.getBytes();
	}
	
	public void save(LinkedList<FingerEntry> list)
	{
		try{
		FileOutputStream fos = context.openFileOutput(filename, context.MODE_PRIVATE);
		byte[] tosave = fString(list);
		fos.write(tosave);
		fos.close();
		} catch(Exception e)
		{
			Toast.makeText(context, R.string.nosave, Toast.LENGTH_SHORT).show();
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}
	
	public LinkedList<FingerEntry> load()
	{
		LinkedList<FingerEntry> ll = new LinkedList<FingerEntry>();
		try{
			FileInputStream fis = context.openFileInput(filename);
			Scanner sc = new Scanner(fis);
			sc.useDelimiter("\n");
			while(sc.hasNext()){
				ll.add(new FingerEntry(sc.next(), adapter, activity));
			}
			sc.close();
		} catch(FileNotFoundException e){
			Toast.makeText(context, R.string.nofile, Toast.LENGTH_SHORT).show();
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
		}
		return ll;
	}
}
