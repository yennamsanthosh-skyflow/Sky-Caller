package com.simplemobiletools.dialer.fragments

import android.content.Context
import android.content.Intent
import android.provider.ContactsContract
import android.util.AttributeSet
import com.reddit.indicatorfastscroll.FastScrollItemIndicator
import com.simplemobiletools.commons.adapters.MyRecyclerViewAdapter
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.MyContactsContentProvider
import com.simplemobiletools.commons.helpers.PERMISSION_READ_CONTACTS
import com.simplemobiletools.commons.helpers.SimpleContactsHelper
import com.simplemobiletools.commons.models.SimpleContact
import com.simplemobiletools.dialer.R
import com.simplemobiletools.dialer.activities.SimpleActivity
import com.simplemobiletools.dialer.adapters.ContactsAdapter
import com.simplemobiletools.dialer.extensions.startContactDetailsIntent
import com.simplemobiletools.dialer.interfaces.RefreshItemsListener
import kotlinx.android.synthetic.main.fragment_letters_layout.view.*
import java.util.*

class ContactsFragment(context: Context, attributeSet: AttributeSet) : MyViewPagerFragment(context, attributeSet), RefreshItemsListener {
    private var allContacts = ArrayList<SimpleContact>()

    override fun setupFragment() {
        val placeholderResId = if (context.hasPermission(PERMISSION_READ_CONTACTS)) {
            R.string.no_contacts_found
        } else {
            R.string.could_not_access_contacts
        }

        fragment_placeholder.text = context.getString(placeholderResId)

        val placeholderActionResId = if (context.hasPermission(PERMISSION_READ_CONTACTS)) {
            R.string.create_new
        } else {
            R.string.request_access
        }

        fragment_placeholder_2.apply {
            text = context.getString(placeholderActionResId)
            underlineText()
            setOnClickListener {
                if (context.hasPermission(PERMISSION_READ_CONTACTS)) {
                    launchCreateNewIntent()
                } else {
                    requestReadContactsPermission()
                }
            }
        }

        fragment_fab.setOnClickListener {
            launchCreateNewIntent()
        }
    }

    override fun setupColors(textColor: Int, primaryColor: Int, adjustedPrimaryColor: Int) {
        (fragment_list?.adapter as? MyRecyclerViewAdapter)?.updateTextColor(textColor)
        fragment_placeholder.setTextColor(textColor)
        fragment_placeholder_2.setTextColor(adjustedPrimaryColor)

        letter_fastscroller.textColor = textColor.getColorStateList()
        letter_fastscroller.pressedTextColor = adjustedPrimaryColor
        letter_fastscroller_thumb.setupWithFastScroller(letter_fastscroller)
        letter_fastscroller_thumb.textColor = adjustedPrimaryColor.getContrastColor()
        letter_fastscroller_thumb.thumbColor = adjustedPrimaryColor.getColorStateList()

        fragment_fab.setColors(
            textColor,
            adjustedPrimaryColor,
            adjustedPrimaryColor.getContrastColor()
        )
    }

    override fun refreshItems() {
        val privateCursor = context?.getMyContactsCursor(false, true)?.loadInBackground()
        SimpleContactsHelper(context).getAvailableContacts(false) { contacts ->
            allContacts = contacts

            val privateContacts = MyContactsContentProvider.getSimpleContacts(context, privateCursor)
            if (privateContacts.isNotEmpty()) {
                allContacts.addAll(privateContacts)
                allContacts.sort()
            }

            activity?.runOnUiThread {
                gotContacts(contacts)
            }
        }
    }

    private fun gotContacts(contacts: ArrayList<SimpleContact>) {
        setupLetterFastscroller(contacts)
        if (contacts.isEmpty()) {
            fragment_placeholder.beVisible()
            fragment_placeholder_2.beVisible()
            fragment_list.beGone()
        } else {
            fragment_placeholder.beGone()
            fragment_placeholder_2.beGone()
            fragment_list.beVisible()

            val currAdapter = fragment_list.adapter
            if (currAdapter == null) {
                ContactsAdapter(activity as SimpleActivity, contacts, fragment_list, this) {
                    val contact = it as SimpleContact
                    activity?.startContactDetailsIntent(contact)
                }.apply {
                    fragment_list.adapter = this
                }

                if (context.areSystemAnimationsEnabled) {
                    fragment_list.scheduleLayoutAnimation()
                }
            } else {
                (currAdapter as ContactsAdapter).updateItems(contacts)
            }
        }
    }

    private fun setupLetterFastscroller(contacts: ArrayList<SimpleContact>) {
        letter_fastscroller.setupWithRecyclerView(fragment_list, { position ->
            try {
                val name = contacts[position].name
                val character = if (name.isNotEmpty()) name.substring(0, 1) else ""
                FastScrollItemIndicator.Text(character.toUpperCase(Locale.getDefault()).normalizeString())
            } catch (e: Exception) {
                FastScrollItemIndicator.Text("")
            }
        })
    }

    override fun onSearchClosed() {
        fragment_placeholder.beVisibleIf(allContacts.isEmpty())
        (fragment_list.adapter as? ContactsAdapter)?.updateItems(allContacts)
        setupLetterFastscroller(allContacts)
    }

    override fun onSearchQueryChanged(text: String) {
        val contacts = allContacts.filter {
            it.doesContainPhoneNumber(text) ||
                it.name.contains(text, true) ||
                it.name.normalizeString().contains(text, true) ||
                it.name.contains(text.normalizeString(), true)
        }.sortedByDescending {
            it.name.startsWith(text, true)
        }.toMutableList() as ArrayList<SimpleContact>

        fragment_placeholder.beVisibleIf(contacts.isEmpty())
        (fragment_list.adapter as? ContactsAdapter)?.updateItems(contacts, text)
        setupLetterFastscroller(contacts)
    }

    private fun requestReadContactsPermission() {
        activity?.handlePermission(PERMISSION_READ_CONTACTS) {
            if (it) {
                fragment_placeholder.text = context.getString(R.string.no_contacts_found)
                fragment_placeholder_2.text = context.getString(R.string.create_new)
                fragment_placeholder_2.setOnClickListener {
                    launchCreateNewIntent()
                }

                SimpleContactsHelper(context).getAvailableContacts(false) { contacts ->
                    activity?.runOnUiThread {
                        gotContacts(contacts)
                    }
                }
            }
        }
    }

    private fun launchCreateNewIntent() {
        Intent().apply {
            action = Intent.ACTION_INSERT
            data = ContactsContract.Contacts.CONTENT_URI
            context.launchActivityIntent(this)
        }
    }
}
