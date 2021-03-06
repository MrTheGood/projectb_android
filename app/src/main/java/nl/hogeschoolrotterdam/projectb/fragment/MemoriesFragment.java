package nl.hogeschoolrotterdam.projectb.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.Snackbar;
import nl.hogeschoolrotterdam.projectb.MemoryDetailActivity;
import nl.hogeschoolrotterdam.projectb.R;
import nl.hogeschoolrotterdam.projectb.WhibApp;
import nl.hogeschoolrotterdam.projectb.adapter.ExpandableListAdapter;
import nl.hogeschoolrotterdam.projectb.adapter.MemoriesAdapter;
import nl.hogeschoolrotterdam.projectb.data.Database;
import nl.hogeschoolrotterdam.projectb.data.room.entities.Memory;
import nl.hogeschoolrotterdam.projectb.model.DataItem;
import nl.hogeschoolrotterdam.projectb.model.SubCategoryItem;
import nl.hogeschoolrotterdam.projectb.util.AnalyticsUtil;
import nl.hogeschoolrotterdam.projectb.util.ConstantManager;

import java.util.*;

/**
 * Created by maartendegoede on 20/03/2019.
 * Copyright © 2019 Anass El Mahdaoui, Hicham El Marzgioui, Wesley de Man, Maarten de Goede all rights reserved.
 */
public class MemoriesFragment extends Fragment {
    private List<Memory> memories;
    private MemoriesAdapter adapter;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private LinearLayoutManager layoutManager;

    private View emptyStateWarning;
    private TextView emptyStateWarningText;

    private ActionModeCallback actionModeCallback;
    private ActionMode actionMode;
    private List<Memory> items = new ArrayList<>();
    private ConstraintLayout coordinatorLayout;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_memories, container, false);
        setHasOptionsMenu(true);

        memories = Database.getInstance().getMemories();
        actionModeCallback = new ActionModeCallback();
        adapter = new MemoriesAdapter(items, new MemoriesAdapter.OnClickListener() {
            @Override
            public void onItemClick(View view, Memory obj, int pos) {
                if (adapter.getSelectedItemCount() > 0) {
                    enableActionMode(pos);
                } else {
                    Intent intent = new Intent(getContext(), MemoryDetailActivity.class);
                    intent.putExtra("EXTRA_SESSION_ID", obj.getId());
                    view.getContext().startActivity(intent);
                    AnalyticsUtil.selectContent(getContext(), "List");
                }
            }

            @Override
            public void onItemLongClick(View view, Memory obj, int pos) {
                enableActionMode(pos);
                ((AppCompatActivity) requireActivity()).getSupportActionBar().hide();
            }
        });

        RecyclerView recyclerView = view.findViewById(R.id.memorylist);
        recyclerView.setAdapter(adapter);

        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        Toolbar tb = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(tb);

        coordinatorLayout = view.findViewById(R.id.coordinatorLayout);
        mDrawerLayout = view.findViewById(R.id.drawer);
        emptyStateWarning = view.findViewById(R.id.empty_list);
        emptyStateWarningText = view.findViewById(R.id.empty_list_text);

        mToggle = new ActionBarDrawerToggle(getActivity(), mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button btn = view.findViewById(R.id.btn);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> selectedYears = new ArrayList<>();
                ArrayList<String> selectedCountries = new ArrayList<>();


                for (int i = 0; i < ExpandableListAdapter.parentItems.size(); i++) {
                    HashMap<String, String> parentItem = ExpandableListAdapter.parentItems.get(i);

                    boolean isLocationList = parentItem.get(ConstantManager.Parameter.CATEGORY_NAME).equals("Location");
                    boolean isYearList = parentItem.get(ConstantManager.Parameter.CATEGORY_NAME).equals("Year");

                    for (int j = 0; j < ExpandableListAdapter.childItems.get(i).size(); j++) {
                        HashMap<String, String> childItem = ExpandableListAdapter.childItems.get(i).get(j);

                        String isChildChecked = childItem.get(ConstantManager.Parameter.IS_CHECKED);
                        if (isChildChecked.equalsIgnoreCase(ConstantManager.CHECK_BOX_CHECKED_TRUE)) {
                            if (isLocationList) {
                                selectedCountries.add(childItem.get(ConstantManager.Parameter.SUB_CATEGORY_NAME));
                            } else if (isYearList) {
                                selectedYears.add(childItem.get(ConstantManager.Parameter.SUB_CATEGORY_NAME));
                            }
                        }
                    }
                }
                if (selectedCountries.size() > 0 || selectedYears.size() > 0) {
                    filter(selectedYears, selectedCountries);
                } else {
                    onResume();
                }

                mDrawerLayout.closeDrawers();

            }
        });

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (actionMode != null) {
            actionMode.finish();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        memories = Database.getInstance().getMemories();
        adapter.setData(memories);
        setupReferences();

        showEmptyState(memories.isEmpty(), getString(R.string.error_no_memories));
    }

    private void showEmptyState(Boolean show) {
        showEmptyState(show, emptyStateWarningText.getText().toString());
    }

    private void showEmptyState(Boolean show, String warningText) {
        emptyStateWarning.setVisibility(show ? View.VISIBLE : View.GONE);
        emptyStateWarningText.setText(warningText);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_memory_listsort, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (mToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.Newest:
                layoutManager.scrollToPositionWithOffset(0, 0);
                Collections.sort(memories, new Comparator<Memory>() {
                    @Override
                    public int compare(Memory a, Memory b) {
                        if (b.getDate().before(a.getDate()))
                            return -1;
                        if (b.getDate().after(a.getDate()))
                            return 1;
                        return 0;
                    }
                });
                adapter.setData(memories);
                return true;
            case R.id.Oldest:
                layoutManager.scrollToPositionWithOffset(0, 0);
                Collections.sort(memories, new Comparator<Memory>() {
                    @Override
                    public int compare(Memory a, Memory b) {
                        if (b.getDate().before(a.getDate()))
                            return 1;
                        if (b.getDate().after(a.getDate()))
                            return -1;
                        return 0;
                    }
                });
                adapter.setData(memories);
                return true;
            case R.id.Alphabetical:
                layoutManager.scrollToPositionWithOffset(0, 0);
                Collections.sort(memories, new Comparator<Memory>() {
                    @Override
                    public int compare(Memory a, Memory b) {
                        return (a.getTitle().toLowerCase().compareTo(b.getTitle().toLowerCase()));
                    }
                });
                adapter.setData(memories);
                return true;

            case R.id.Country:
                layoutManager.scrollToPositionWithOffset(0, 0);
                Collections.sort(memories, new Comparator<Memory>() {
                    @Override
                    public int compare(Memory a, Memory b) {
                        return (a.getCountryName(getContext()).toLowerCase().compareTo(b.getCountryName(getContext()).toLowerCase()));
                    }
                });
                adapter.setData(memories);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupReferences() {

        ExpandableListView lvCategory = requireView().findViewById(R.id.lvCategory);
        ArrayList<DataItem> arCategory = new ArrayList<>();
        ArrayList<SubCategoryItem> arSubCategory;
        ArrayList<HashMap<String, String>> parentItems = new ArrayList<>();
        ArrayList<ArrayList<HashMap<String, String>>> childItems = new ArrayList<>();


        DataItem dataItem = new DataItem();
        dataItem.setCategoryId("1");
        dataItem.setCategoryName("Year");
        arSubCategory = new ArrayList<>();
        ArrayList<String> filter_memories_1 = new ArrayList<String>();


        for (int j = 0; j < memories.size(); j++) {
            String year = memories.get(j).getYear();
            if (!filter_memories_1.contains(year))
                filter_memories_1.add(year);
        }

        int k = 0;
        for (String item : filter_memories_1) {
            SubCategoryItem subCategoryItem = new SubCategoryItem();
            subCategoryItem.setCategoryId(String.valueOf(k++));
            subCategoryItem.setIsChecked(ConstantManager.CHECK_BOX_CHECKED_FALSE);
            subCategoryItem.setSubCategoryName(item);
            arSubCategory.add(subCategoryItem);
        }

        dataItem.setSubCategory(arSubCategory);
        arCategory.add(dataItem);

        dataItem = new DataItem();
        dataItem.setCategoryId("2");
        dataItem.setCategoryName("Location");
        arSubCategory = new ArrayList<>();
        ArrayList<String> filter_memories_2 = new ArrayList<String>();

        for (int j = 0; j < memories.size(); j++) {
            String country = memories.get(j).getCountryName(getContext());
            if (!filter_memories_2.contains(country))
                filter_memories_2.add(country);
        }

        int i = 0;
        for (String item : filter_memories_2) {
            SubCategoryItem subCategoryItem = new SubCategoryItem();
            subCategoryItem.setCategoryId(String.valueOf(i++));
            subCategoryItem.setIsChecked(ConstantManager.CHECK_BOX_CHECKED_FALSE);
            subCategoryItem.setSubCategoryName(item);
            arSubCategory.add(subCategoryItem);
        }

        dataItem.setSubCategory(arSubCategory);
        arCategory.add(dataItem);

        for (DataItem data : arCategory) {

            ArrayList<HashMap<String, String>> childArrayList = new ArrayList<>();
            HashMap<String, String> mapParent = new HashMap<>();

            mapParent.put(ConstantManager.Parameter.CATEGORY_ID, data.getCategoryId());
            mapParent.put(ConstantManager.Parameter.CATEGORY_NAME, data.getCategoryName());

            int countIsChecked = 0;
            for (SubCategoryItem subCategoryItem : data.getSubCategory()) {

                HashMap<String, String> mapChild = new HashMap<>();
                mapChild.put(ConstantManager.Parameter.SUB_ID, subCategoryItem.getSubId());
                mapChild.put(ConstantManager.Parameter.SUB_CATEGORY_NAME, subCategoryItem.getSubCategoryName());
                mapChild.put(ConstantManager.Parameter.CATEGORY_ID, subCategoryItem.getCategoryId());
                mapChild.put(ConstantManager.Parameter.IS_CHECKED, subCategoryItem.getIsChecked());

                if (subCategoryItem.getIsChecked().equalsIgnoreCase(ConstantManager.CHECK_BOX_CHECKED_TRUE)) {
                    countIsChecked++;
                }
                childArrayList.add(mapChild);
            }

            if (countIsChecked == data.getSubCategory().size()) {

                data.setIsChecked(ConstantManager.CHECK_BOX_CHECKED_TRUE);
            } else {
                data.setIsChecked(ConstantManager.CHECK_BOX_CHECKED_FALSE);
            }

            mapParent.put(ConstantManager.Parameter.IS_CHECKED, data.getIsChecked());
            childItems.add(childArrayList);
            parentItems.add(mapParent);

        }

        ConstantManager.parentItems = parentItems;
        ConstantManager.childItems = childItems;

        ExpandableListAdapter expandableListAdapter = new ExpandableListAdapter(requireActivity(), parentItems, childItems);
        lvCategory.setAdapter(expandableListAdapter);


    }

    private void filter(ArrayList<String> selectedYears, ArrayList<String> selectedCountries) {

        ArrayList<Memory> filteredlist = new ArrayList<>();

        for (Memory item : memories) {
            if (selectedYears.size() == 0 || selectedCountries.size() == 0) {
                if (selectedYears.contains(item.getYear())) {
                    filteredlist.add(item);
                } else if (selectedCountries.contains(item.getCountryName(getContext()))) {
                    filteredlist.add(item);
                }
            } else if (selectedYears.contains(item.getYear())
                    && selectedCountries.contains(item.getCountryName(getContext()))) {
                filteredlist.add(item);
            }
        }

        adapter.setData(filteredlist);
        showEmptyState(filteredlist.isEmpty(), getString(R.string.error_no_filter_match));
        AnalyticsUtil.search(getContext());
    }


    private void enableActionMode(int position) {
        if (actionMode == null) {
            actionMode = ((AppCompatActivity) requireActivity()).startSupportActionMode(actionModeCallback);
        }
        toggleSelection(position);
    }

    private void toggleSelection(int position) {
        adapter.toggleSelection(position);
        int count = adapter.getSelectedItemCount();

        if (count == 0) {
            actionMode.finish();
        } else {
            actionMode.setTitle(String.valueOf(count));
            actionMode.invalidate();
        }
    }

    private class ActionModeCallback implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_delete, menu);
            WhibApp.getInstance().tintMenuItems(menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int id = item.getItemId();
            if (id == R.id.action_delete) {
                deleteMemories();
                mode.finish();
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            adapter.clearSelections();
            actionMode = null;
            ((AppCompatActivity) requireActivity()).getSupportActionBar().show();
        }
    }

    private void deleteMemories() {
        final List<Memory> selectedMemories = adapter.getSelectedItems();
        for (Memory m : selectedMemories) {
            adapter.removeData(m);
            Database.getInstance().deleteMemory(m);
        }

        showEmptyState(adapter.getItemCount() == 0);
        Snackbar.make(coordinatorLayout, R.string.snackbar_description, Snackbar.LENGTH_LONG)
                .setAction(R.string.snackbar_undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for (Memory m : selectedMemories) {
                            Database.getInstance().addMemory(m);
                        }
                        adapter.setData(Database.getInstance().getMemories());
                        showEmptyState(adapter.getItemCount() == 0);
                    }
                })
                .setActionTextColor(Color.RED)
                .show();


        adapter.notifyDataSetChanged();
    }

}


