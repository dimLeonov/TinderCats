
/* Created by Natalia 16.07.19 */

package com.tindercatapp.myapplication;

import android.content.Context;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;

import org.junit.runners.MethodSorters;
import org.mockito.*;

import java.util.List;

// Run test methods in alphabetical order
@FixMethodOrder(MethodSorters.NAME_ASCENDING)

public class arrayAdapterTest
{
    @Mock
    arrayAdapter arrayAdapter;
    Context mContext;
    List<cards> cardsList;

    //cards card,card2;
    //int position;
    //View convertsView;
    //ViewGroup parent;
    //View view;

    @Before
    public void setUp()
    {

    }
    @Test
    public void test1Constructor ()
    {
        try
        {
            // positive test
            new arrayAdapter(mContext, R.layout.item, cardsList);
            // invalid resource id
            new arrayAdapter(mContext, -1, cardsList);
            // empty cards List
            new arrayAdapter(mContext, R.layout.item, null);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    @Test
    public void test2ConstructorWithNullContext () throws NullPointerException
    {
        new arrayAdapter(null, R.layout.item, cardsList );
    }

    @Test
   public void test3getView()
    {
       // arrayAdapter ad=new arrayAdapter(mContext,0,null);
       // View view=ad.getView(R.layout.item, convertsView, parent);
       // TextView name=(TextView)view.findViewById(R.id.name);

       // TextView name = (TextView) view.findViewById(R.id.name);
        //ImageView image = (ImageView) view.findViewById(R.id.image);

       // name.setText(card.getName());
        //image.setImageResource(R.mipmap.ic_launcher);

        //View view=(ImageView) convertsView.findViewById(R.id.image);
        //ListView parent = new ListView(mContext);

        // ArrayList<cards> cardsdd=new ArrayList<cards>();
        // cards card1 = new cards("1","test");
        // cardsdd.add(card1);
        // String[] str={"test"};


        //view = null;
        //Assert.assertEquals(null,arrayAdapter.getView(0,null,null));
        //TextView name=(TextView)view.findViewById(R.id.name);
        // assertThat(arrayAdapter.getView(0,null,null)),isNotNull();
        // arrayAdapter.setRetainViewMode();
        // assertNull(arrayAdapter.getView(0,null,null));
    }
}