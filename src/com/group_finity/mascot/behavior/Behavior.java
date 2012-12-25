package com.group_finity.mascot.behavior;

import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.exception.CantBeAliveException;

import java.awt.event.MouseEvent;


/**
 * マスコットの長期的な振る舞いをあらわすオブジェクト.
 * <p/>
 * {@link Mascot#setBehavior(Behavior)} で使用する.
 */
public interface Behavior {

    /**
     * 行動を開始する時に呼び出す.
     *
     * @param mascot 関連付けるマスコット.
     */
    public void init(Mascot mascot) throws CantBeAliveException;

    /**
     * マスコットを次のコマに進める.
     */
    public void next() throws CantBeAliveException;

    public void mousePressed(MouseEvent e) throws CantBeAliveException;

    public void mouseReleased(MouseEvent e) throws CantBeAliveException;
}
