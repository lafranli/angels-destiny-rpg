package fantasy;
/*
Copyright (C) 2012 Johan Ceuppens

This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
*/
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import javax.swing.ImageIcon;
import java.awt.Toolkit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import java.util.*;

class BattleGrid 
{

protected LinkedList grid = new LinkedList();
protected int size = 6;

public BattleGrid(int sz)
{
	size = sz;

	int i;
	for (i = 0; i < sz; i++)
		grid.add("none");
}

public void set(int xx, int yy, String monstername)
{

	LinkedList backupgrid = new LinkedList();
	int i;
	for (i = 0; i < size; i++)
		backupgrid.add(grid.get(i));

	grid.clear();

	int index = xx + yy*size/2;

	for (i = 0; i < index; i++)
		grid.add(backupgrid.get(i));

	grid.add(monstername);

	for (i = index; i < size; i++)
		grid.add(grid.get(i));
}

public String get(int xx, int yy)
{
	Object o = grid.get(yy*size/2+xx);
	String s = (String)o;
	return s;
}

public int getsizex()
{
	return size/2;// == 3
}

public int getsizey()
{
	return size/3;// == 2
}

};